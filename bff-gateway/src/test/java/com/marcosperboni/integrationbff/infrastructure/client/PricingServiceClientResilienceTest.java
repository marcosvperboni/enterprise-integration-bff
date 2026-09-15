package com.marcosperboni.integrationbff.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

/**
 * Exercises the real Resilience4j retry + circuit breaker wiring against a
 * WireMock stand-in for external-pricing-api-mock, instead of just asserting
 * the annotations are present.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PricingServiceClientResilienceTest {

    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    private PricingServiceClient pricingServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void registerDownstreamUrl(DynamicPropertyRegistry registry) {
        registry.add("app.clients.pricing-service.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetStubsAndCircuitBreaker() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("pricingService").reset();
    }

    @Test
    void succeedsWithoutRetryWhenDownstreamIsHealthy() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/pricing/prod-2001")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"productId\":\"prod-2001\",\"currency\":\"USD\",\"amount\":89.90,\"discountPercentage\":10}")));

        StepVerifier.create(pricingServiceClient.getPrice("prod-2001", null))
                .assertNext(price -> {
                    org.assertj.core.api.Assertions.assertThat(price.currency()).isEqualTo("USD");
                    org.assertj.core.api.Assertions.assertThat(price.discountPercentage()).isEqualTo(10);
                })
                .verifyComplete();
    }

    @Test
    void retriesOn5xxThenFallsBackToDownstreamUnavailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/pricing/prod-2001")).willReturn(aResponse().withStatus(500)));

        StepVerifier.create(pricingServiceClient.getPrice("prod-2001", null))
                .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                .verify();

        wireMockServer.verify(moreThanOrExactly(2), getRequestedFor(urlPathEqualTo("/api/pricing/prod-2001")));
    }

    @Test
    void timesOutAndFallsBackToDownstreamUnavailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/pricing/prod-2001")).willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(5000)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"productId\":\"prod-2001\",\"currency\":\"USD\",\"amount\":1,\"discountPercentage\":0}")));

        StepVerifier.create(pricingServiceClient.getPrice("prod-2001", null))
                .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                .verify();
    }
}
