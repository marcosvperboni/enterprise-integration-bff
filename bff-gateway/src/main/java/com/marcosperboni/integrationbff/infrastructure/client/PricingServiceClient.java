package com.marcosperboni.integrationbff.infrastructure.client;

import com.marcosperboni.integrationbff.config.properties.ClientsProperties;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import com.marcosperboni.integrationbff.domain.port.PricingClientPort;
import com.marcosperboni.integrationbff.infrastructure.client.dto.PricingUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PricingServiceClient implements PricingClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public PricingServiceClient(WebClient pricingServiceWebClient, ClientsProperties properties) {
        this.webClient = pricingServiceWebClient;
        this.timeout = Duration.ofMillis(properties.pricingService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "fallback")
    @Retry(name = "pricingService")
    public Mono<PriceInfo> getPrice(String productId, String authorization) {
        return webClient.get()
                .uri("/api/pricing/{productId}", productId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .bodyToMono(PricingUpstreamDto.class)
                .timeout(timeout)
                .map(PricingUpstreamDto::toPriceInfo);
    }

    private void applyAuth(HttpHeaders headers, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    @SuppressWarnings("unused")
    private Mono<PriceInfo> fallback(String productId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("external-pricing-api", ex));
    }
}
