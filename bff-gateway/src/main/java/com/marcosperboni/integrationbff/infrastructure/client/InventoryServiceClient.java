package com.marcosperboni.integrationbff.infrastructure.client;

import com.marcosperboni.integrationbff.config.properties.ClientsProperties;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import com.marcosperboni.integrationbff.domain.port.InventoryClientPort;
import com.marcosperboni.integrationbff.infrastructure.client.dto.LegacyInventoryUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryServiceClient implements InventoryClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public InventoryServiceClient(WebClient inventoryServiceWebClient, ClientsProperties properties) {
        this.webClient = inventoryServiceWebClient;
        this.timeout = Duration.ofMillis(properties.inventoryService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    @Retry(name = "inventoryService")
    public Mono<InventoryInfo> getInventory(String productId, String authorization) {
        return webClient.get()
                .uri("/legacy/inventory/{productId}", productId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .bodyToMono(LegacyInventoryUpstreamDto.class)
                .timeout(timeout)
                .map(LegacyInventoryUpstreamDto::toInventoryInfo);
    }

    private void applyAuth(HttpHeaders headers, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    @SuppressWarnings("unused")
    private Mono<InventoryInfo> fallback(String productId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("legacy-inventory-service", ex));
    }
}
