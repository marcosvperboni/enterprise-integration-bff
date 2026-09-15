package com.marcosperboni.integrationbff.application;

import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import com.marcosperboni.integrationbff.domain.model.ProductAggregate;
import com.marcosperboni.integrationbff.domain.port.CatalogClientPort;
import com.marcosperboni.integrationbff.domain.port.InventoryClientPort;
import com.marcosperboni.integrationbff.domain.port.PricingClientPort;
import com.marcosperboni.integrationbff.infrastructure.cache.ProductAggregateCacheService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the three downstream calls behind a single response. The
 * catalog lookup is mandatory - a product with no identity is meaningless, so
 * a catalog failure fails the whole call. Pricing and inventory degrade
 * gracefully to null plus a warning so a partial outage in either enrichment
 * source never turns into a hard error for the client.
 */
@Service
public class ProductAggregationService {

    private static final int LIST_FAN_OUT_CONCURRENCY = 8;

    private final CatalogClientPort catalogClient;
    private final PricingClientPort pricingClient;
    private final InventoryClientPort inventoryClient;
    private final ProductAggregateCacheService cacheService;

    public ProductAggregationService(CatalogClientPort catalogClient, PricingClientPort pricingClient,
            InventoryClientPort inventoryClient, ProductAggregateCacheService cacheService) {
        this.catalogClient = catalogClient;
        this.pricingClient = pricingClient;
        this.inventoryClient = inventoryClient;
        this.cacheService = cacheService;
    }

    public Mono<ProductAggregate> getProduct(String productId, String authorization) {
        return cacheService.get(productId)
                .switchIfEmpty(Mono.defer(() -> catalogClient.getProduct(productId, authorization)
                        .flatMap(catalog -> buildAggregate(catalog, authorization))
                        .flatMap(aggregate -> cacheService.put(productId, aggregate).thenReturn(aggregate))));
    }

    public Flux<ProductAggregate> listProducts(String authorization) {
        return catalogClient.listProducts(authorization)
                .flatMapMany(Flux::fromIterable)
                .flatMap(catalog -> getProduct(catalog.productId(), authorization), LIST_FAN_OUT_CONCURRENCY);
    }

    private Mono<ProductAggregate> buildAggregate(CatalogInfo catalog, String authorization) {
        List<String> warnings = new CopyOnWriteArrayList<>();

        Mono<Optional<PriceInfo>> price = pricingClient.getPrice(catalog.productId(), authorization)
                .map(Optional::of)
                .onErrorResume(DownstreamUnavailableException.class, ex -> {
                    warnings.add("pricing unavailable: " + ex.getMessage());
                    return Mono.just(Optional.empty());
                });

        Mono<Optional<InventoryInfo>> inventory = inventoryClient.getInventory(catalog.productId(), authorization)
                .map(Optional::of)
                .onErrorResume(DownstreamUnavailableException.class, ex -> {
                    warnings.add("inventory unavailable: " + ex.getMessage());
                    return Mono.just(Optional.empty());
                });

        return Mono.zip(price, inventory)
                .map(tuple -> new ProductAggregate(catalog, tuple.getT1().orElse(null), tuple.getT2().orElse(null),
                        List.copyOf(warnings)));
    }
}
