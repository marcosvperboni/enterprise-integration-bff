package com.marcosperboni.integrationbff.domain.port;

import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import java.util.List;
import reactor.core.publisher.Mono;

public interface CatalogClientPort {

    Mono<List<CatalogInfo>> listProducts(String authorization);

    Mono<CatalogInfo> getProduct(String productId, String authorization);

    Mono<CatalogInfo> createProduct(CatalogInfo product, String authorization);

    Mono<CatalogInfo> updateProduct(String productId, CatalogInfo product, String authorization);

    Mono<Void> deleteProduct(String productId, String authorization);
}
