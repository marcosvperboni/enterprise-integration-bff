package com.marcosperboni.integrationbff.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.LocalDate;
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
 * WireMock stand-in for legacy-inventory-service-mock, and confirms the
 * quirky legacy JSON shape is normalized end-to-end through the WebClient
 * adapter.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InventoryServiceClientResilienceTest {

    static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

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
        registry.add("app.clients.inventory-service.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetStubsAndCircuitBreaker() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("inventoryService").reset();
    }

    @Test
    void normalizesQuirkyLegacyShapeIntoCleanDomainModel() {
        wireMockServer.stubFor(get(urlPathEqualTo("/legacy/inventory/prod-2001")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"PROD_ID\":\"prod-2001\",\"QTY_AVAIL\":\"120\",\"WHS_CD\":\"WH-01\",\"LAST_UPD_DT\":\"20260910\"}")));

        StepVerifier.create(inventoryServiceClient.getInventory("prod-2001", null))
                .assertNext(inventory -> {
                    assertThat(inventory.quantityAvailable()).isEqualTo(120);
                    assertThat(inventory.warehouseCode()).isEqualTo("WH-01");
                    assertThat(inventory.lastUpdated()).isEqualTo(LocalDate.of(2026, 9, 10));
                })
                .verifyComplete();
    }

    @Test
    void retriesOn5xxThenFallsBackToDownstreamUnavailable() {
        wireMockServer.stubFor(get(urlPathEqualTo("/legacy/inventory/prod-2001")).willReturn(aResponse().withStatus(500)));

        StepVerifier.create(inventoryServiceClient.getInventory("prod-2001", null))
                .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                .verify();

        wireMockServer.verify(moreThanOrExactly(2), getRequestedFor(urlPathEqualTo("/legacy/inventory/prod-2001")));
    }

    @Test
    void retryExhaustionEventuallyOpensCircuitBreaker() {
        wireMockServer.stubFor(get(urlPathEqualTo("/legacy/inventory/prod-2001")).willReturn(aResponse().withStatus(503)));

        for (int i = 0; i < 6; i++) {
            StepVerifier.create(inventoryServiceClient.getInventory("prod-2001", null))
                    .expectErrorMatches(DownstreamUnavailableException.class::isInstance)
                    .verify();
        }

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
