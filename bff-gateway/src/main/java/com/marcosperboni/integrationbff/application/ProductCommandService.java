package com.marcosperboni.integrationbff.application;

import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.port.CatalogClientPort;
import com.marcosperboni.integrationbff.infrastructure.cache.ProductAggregateCacheService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * The internal catalog system is the product's source of truth, so writes go
 * there only - pricing and inventory are enrichment data owned by their
 * respective systems, not by this BFF. Any successful write evicts the
 * aggregate cache entry so the next read reflects the change immediately.
 */
@Service
public class ProductCommandService {

    private final CatalogClientPort catalogClient;
    private final ProductAggregateCacheService cacheService;

    public ProductCommandService(CatalogClientPort catalogClient, ProductAggregateCacheService cacheService) {
        this.catalogClient = catalogClient;
        this.cacheService = cacheService;
    }

    public Mono<CatalogInfo> create(CatalogInfo product, String authorization) {
        return catalogClient.createProduct(product, authorization);
    }

    public Mono<CatalogInfo> update(String productId, CatalogInfo product, String authorization) {
        return catalogClient.updateProduct(productId, product, authorization)
                .flatMap(updated -> cacheService.evict(productId).thenReturn(updated));
    }

    public Mono<Void> delete(String productId, String authorization) {
        return catalogClient.deleteProduct(productId, authorization)
                .then(cacheService.evict(productId))
                .then();
    }
}
