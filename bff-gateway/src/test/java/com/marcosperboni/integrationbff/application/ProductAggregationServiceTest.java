package com.marcosperboni.integrationbff.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.marcosperboni.integrationbff.domain.exception.DownstreamUnavailableException;
import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import com.marcosperboni.integrationbff.domain.model.ProductAggregate;
import com.marcosperboni.integrationbff.domain.port.CatalogClientPort;
import com.marcosperboni.integrationbff.domain.port.InventoryClientPort;
import com.marcosperboni.integrationbff.domain.port.PricingClientPort;
import com.marcosperboni.integrationbff.infrastructure.cache.ProductAggregateCacheService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductAggregationServiceTest {

    private static final CatalogInfo CATALOG = new CatalogInfo("prod-2001", "Keyboard", "desc", "Electronics", true);
    private static final PriceInfo PRICE = new PriceInfo("USD", new BigDecimal("89.90"), 10);
    private static final InventoryInfo INVENTORY = new InventoryInfo(120, "WH-01", LocalDate.of(2026, 9, 10));

    private CatalogClientPort catalogClient;
    private PricingClientPort pricingClient;
    private InventoryClientPort inventoryClient;
    private ProductAggregateCacheService cacheService;
    private ProductAggregationService service;

    @BeforeEach
    void setUp() {
        catalogClient = mock(CatalogClientPort.class);
        pricingClient = mock(PricingClientPort.class);
        inventoryClient = mock(InventoryClientPort.class);
        cacheService = mock(ProductAggregateCacheService.class);
        service = new ProductAggregationService(catalogClient, pricingClient, inventoryClient, cacheService);

        given(cacheService.get(any())).willReturn(Mono.empty());
        given(cacheService.put(any(), any())).willReturn(Mono.just(true));
    }

    @Test
    void aggregatesAllThreeDownstreamsWhenAllHealthy() {
        given(catalogClient.getProduct("prod-2001", null)).willReturn(Mono.just(CATALOG));
        given(pricingClient.getPrice("prod-2001", null)).willReturn(Mono.just(PRICE));
        given(inventoryClient.getInventory("prod-2001", null)).willReturn(Mono.just(INVENTORY));

        StepVerifier.create(service.getProduct("prod-2001", null))
                .assertNext(aggregate -> {
                    assertThat(aggregate.catalog()).isEqualTo(CATALOG);
                    assertThat(aggregate.price()).isEqualTo(PRICE);
                    assertThat(aggregate.inventory()).isEqualTo(INVENTORY);
                    assertThat(aggregate.warnings()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void fallsBackWithWarningWhenPricingIsDown() {
        given(catalogClient.getProduct("prod-2001", null)).willReturn(Mono.just(CATALOG));
        given(pricingClient.getPrice("prod-2001", null))
                .willReturn(Mono.error(new DownstreamUnavailableException("external-pricing-api", new RuntimeException("boom"))));
        given(inventoryClient.getInventory("prod-2001", null)).willReturn(Mono.just(INVENTORY));

        StepVerifier.create(service.getProduct("prod-2001", null))
                .assertNext(aggregate -> {
                    assertThat(aggregate.price()).isNull();
                    assertThat(aggregate.inventory()).isEqualTo(INVENTORY);
                    assertThat(aggregate.warnings()).hasSize(1);
                    assertThat(aggregate.warnings().getFirst()).contains("pricing unavailable");
                })
                .verifyComplete();
    }

    @Test
    void fallsBackWithWarningsWhenBothEnrichmentDownstreamsAreDown() {
        given(catalogClient.getProduct("prod-2001", null)).willReturn(Mono.just(CATALOG));
        given(pricingClient.getPrice("prod-2001", null))
                .willReturn(Mono.error(new DownstreamUnavailableException("external-pricing-api", new RuntimeException("boom"))));
        given(inventoryClient.getInventory("prod-2001", null))
                .willReturn(Mono.error(new DownstreamUnavailableException("legacy-inventory-service", new RuntimeException("boom"))));

        StepVerifier.create(service.getProduct("prod-2001", null))
                .assertNext(aggregate -> {
                    assertThat(aggregate.catalog()).isEqualTo(CATALOG);
                    assertThat(aggregate.price()).isNull();
                    assertThat(aggregate.inventory()).isNull();
                    assertThat(aggregate.warnings()).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void propagatesErrorWhenCatalogIsDownBecauseIdentityIsMandatory() {
        given(catalogClient.getProduct("prod-2001", null))
                .willReturn(Mono.error(new DownstreamUnavailableException("internal-catalog-api", new RuntimeException("boom"))));

        StepVerifier.create(service.getProduct("prod-2001", null))
                .expectError(DownstreamUnavailableException.class)
                .verify();
    }

    @Test
    void getProductReturnsCachedAggregateWithoutCallingDownstreams() {
        ProductAggregate cached = new ProductAggregate(CATALOG, PRICE, INVENTORY, List.of());
        given(cacheService.get("prod-2001")).willReturn(Mono.just(cached));

        StepVerifier.create(service.getProduct("prod-2001", null))
                .expectNext(cached)
                .verifyComplete();

        org.mockito.Mockito.verifyNoInteractions(catalogClient, pricingClient, inventoryClient);
    }

    @Test
    void listProductsFansOutAcrossAllCatalogEntries() {
        CatalogInfo other = new CatalogInfo("prod-2002", "Chair", "desc", "Furniture", true);
        given(catalogClient.listProducts(null)).willReturn(Mono.just(List.of(CATALOG, other)));
        given(catalogClient.getProduct("prod-2001", null)).willReturn(Mono.just(CATALOG));
        given(catalogClient.getProduct("prod-2002", null)).willReturn(Mono.just(other));
        given(pricingClient.getPrice(any(), any())).willReturn(Mono.just(PRICE));
        given(inventoryClient.getInventory(any(), any())).willReturn(Mono.just(INVENTORY));

        StepVerifier.create(service.listProducts(null).collectList())
                .assertNext(aggregates -> assertThat(aggregates).hasSize(2))
                .verifyComplete();
    }
}
