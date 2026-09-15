package com.marcosperboni.integrationbff.infrastructure.client;

import com.marcosperboni.integrationbff.config.properties.ClientsProperties;
import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.exception.ProductNotFoundException;
import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.port.CatalogClientPort;
import com.marcosperboni.integrationbff.infrastructure.client.dto.CatalogUpstreamDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CatalogServiceClient implements CatalogClientPort {

    private final WebClient webClient;
    private final Duration timeout;

    public CatalogServiceClient(WebClient catalogServiceWebClient, ClientsProperties properties) {
        this.webClient = catalogServiceWebClient;
        this.timeout = Duration.ofMillis(properties.catalogService().timeoutMillis());
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackList")
    @Retry(name = "catalogService")
    public Mono<List<CatalogInfo>> listProducts(String authorization) {
        return webClient.get()
                .uri("/api/catalog/products")
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .bodyToFlux(CatalogUpstreamDto.class)
                .timeout(timeout)
                .map(CatalogUpstreamDto::toCatalogInfo)
                .collectList();
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallback")
    @Retry(name = "catalogService")
    public Mono<CatalogInfo> getProduct(String productId, String authorization) {
        return webClient.get()
                .uri("/api/catalog/products/{id}", productId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new ProductNotFoundException(productId)))
                .bodyToMono(CatalogUpstreamDto.class)
                .timeout(timeout)
                .map(CatalogUpstreamDto::toCatalogInfo);
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallback")
    @Retry(name = "catalogService")
    public Mono<CatalogInfo> createProduct(CatalogInfo product, String authorization) {
        return webClient.post()
                .uri("/api/catalog/products")
                .headers(headers -> applyAuth(headers, authorization))
                .bodyValue(CatalogUpstreamDto.WriteRequest.fromCatalogInfo(product))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.createException())
                .bodyToMono(CatalogUpstreamDto.class)
                .timeout(timeout)
                .map(CatalogUpstreamDto::toCatalogInfo);
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallback")
    @Retry(name = "catalogService")
    public Mono<CatalogInfo> updateProduct(String productId, CatalogInfo product, String authorization) {
        return webClient.put()
                .uri("/api/catalog/products/{id}", productId)
                .headers(headers -> applyAuth(headers, authorization))
                .bodyValue(CatalogUpstreamDto.WriteRequest.fromCatalogInfo(product))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new ProductNotFoundException(productId)))
                .bodyToMono(CatalogUpstreamDto.class)
                .timeout(timeout)
                .map(CatalogUpstreamDto::toCatalogInfo);
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackVoid")
    @Retry(name = "catalogService")
    public Mono<Void> deleteProduct(String productId, String authorization) {
        return webClient.delete()
                .uri("/api/catalog/products/{id}", productId)
                .headers(headers -> applyAuth(headers, authorization))
                .retrieve()
                .onStatus(status -> status.value() == 404, resp -> Mono.error(new ProductNotFoundException(productId)))
                .toBodilessEntity()
                .timeout(timeout)
                .then();
    }

    private void applyAuth(HttpHeaders headers, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    @SuppressWarnings("unused")
    private Mono<List<CatalogInfo>> fallbackList(String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("internal-catalog-api", ex));
    }

    @SuppressWarnings("unused")
    private Mono<CatalogInfo> fallback(String productId, String authorization, ProductNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<CatalogInfo> fallback(String productId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("internal-catalog-api", ex));
    }

    @SuppressWarnings("unused")
    private Mono<CatalogInfo> fallback(CatalogInfo product, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("internal-catalog-api", ex));
    }

    @SuppressWarnings("unused")
    private Mono<CatalogInfo> fallback(String productId, CatalogInfo product, String authorization, ProductNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<CatalogInfo> fallback(String productId, CatalogInfo product, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("internal-catalog-api", ex));
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallbackVoid(String productId, String authorization, ProductNotFoundException ex) {
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallbackVoid(String productId, String authorization, Throwable ex) {
        return Mono.error(new DownstreamUnavailableException("internal-catalog-api", ex));
    }
}
