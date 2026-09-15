package com.marcosperboni.integrationbff.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.exception.ProductNotFoundException;
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
 * WireMock stand-in for internal-catalog-api-mock. Unlike pricing/inventory,
 * a 404 here must not be retried - it means the product genuinely does not
 * exist, not that the downstream is flaky.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CatalogServiceClientResilienceTest {

    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    private CatalogServiceClient catalogServiceClient;

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
        registry.add("app.clients.catalog-service.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetStubsAndCircuitBreaker() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("catalogService").reset();
    }

    @Test
    void succeedsWithoutRetryWhenDownstreamIsHealthy() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/catalog/products/prod-2001")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"prod-2001\",\"name\":\"Keyboard\",\"description\":\"desc\",\"category\":\"Electronics\",\"active\":true}")));

        StepVerifier.create(catalogServiceClient.getProduct("prod-2001", null))
                .assertNext(catalog -> org.assertj.core.api.Assertions.assertThat(catalog.name()).isEqualTo("Keyboard"))
                .verifyComplete();
    }

    @Test
    void doesNotRetryOn404AndPropagatesProductNotFound() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/catalog/products/missing")).willReturn(aResponse().withStatus(404)));

        StepVerifier.create(catalogServiceClient.getProduct("missing", null))
                .expectErrorMatches(ProductNotFoundException.class::isInstance)
                .verify();

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/api/catalog/products/missing")));
    }

    @Test
    void retriesOn5xxThenFallsBackToDownstreamUnavailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/catalog/products/prod-2001")).willReturn(aResponse().withStatus(500)));

        StepVerifier.create(catalogServiceClient.getProduct("prod-2001", null))
                .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                .verify();

        wireMockServer.verify(moreThanOrExactly(2), getRequestedFor(urlPathEqualTo("/api/catalog/products/prod-2001")));
    }
}
