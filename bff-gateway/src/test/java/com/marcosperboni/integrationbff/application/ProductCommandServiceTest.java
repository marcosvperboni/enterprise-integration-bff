package com.marcosperboni.integrationbff.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.port.CatalogClientPort;
import com.marcosperboni.integrationbff.infrastructure.cache.ProductAggregateCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductCommandServiceTest {

    private static final CatalogInfo CATALOG = new CatalogInfo("prod-2001", "Keyboard", "desc", "Electronics", true);

    private CatalogClientPort catalogClient;
    private ProductAggregateCacheService cacheService;
    private ProductCommandService service;

    @BeforeEach
    void setUp() {
        catalogClient = mock(CatalogClientPort.class);
        cacheService = mock(ProductAggregateCacheService.class);
        service = new ProductCommandService(catalogClient, cacheService);
    }

    @Test
    void createDelegatesToCatalogClientWithoutTouchingCache() {
        given(catalogClient.createProduct(CATALOG, null)).willReturn(Mono.just(CATALOG));

        StepVerifier.create(service.create(CATALOG, null)).expectNext(CATALOG).verifyComplete();

        org.mockito.Mockito.verifyNoInteractions(cacheService);
    }

    @Test
    void updateEvictsCacheAfterSuccessfulWrite() {
        given(catalogClient.updateProduct("prod-2001", CATALOG, null)).willReturn(Mono.just(CATALOG));
        given(cacheService.evict("prod-2001")).willReturn(Mono.just(true));

        StepVerifier.create(service.update("prod-2001", CATALOG, null)).expectNext(CATALOG).verifyComplete();

        verify(cacheService).evict("prod-2001");
    }

    @Test
    void deleteEvictsCacheAfterSuccessfulDelete() {
        given(catalogClient.deleteProduct("prod-2001", null)).willReturn(Mono.empty());
        given(cacheService.evict("prod-2001")).willReturn(Mono.just(true));

        StepVerifier.create(service.delete("prod-2001", null)).verifyComplete();

        verify(cacheService).evict("prod-2001");
    }
}
