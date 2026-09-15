package com.marcosperboni.integrationbff.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.marcosperboni.integrationbff.application.ProductAggregationService;
import com.marcosperboni.integrationbff.application.ProductCommandService;
import com.marcosperboni.integrationbff.domain.exception.ProductNotFoundException;
import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import com.marcosperboni.integrationbff.domain.model.ProductAggregate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ProductController.class, excludeAutoConfiguration = ReactiveWebSecurityAutoConfiguration.class)
class ProductControllerTest {

    private static final CatalogInfo CATALOG = new CatalogInfo("prod-2001", "Keyboard", "desc", "Electronics", true);
    private static final PriceInfo PRICE = new PriceInfo("USD", new BigDecimal("89.90"), 10);
    private static final InventoryInfo INVENTORY = new InventoryInfo(120, "WH-01", LocalDate.of(2026, 9, 10));

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductAggregationService aggregationService;

    @MockitoBean
    private ProductCommandService commandService;

    @Test
    void getProductReturnsFullyAggregatedView() {
        ProductAggregate aggregate = new ProductAggregate(CATALOG, PRICE, INVENTORY, List.of());
        given(aggregationService.getProduct(eq("prod-2001"), any())).willReturn(Mono.just(aggregate));

        webTestClient.get().uri("/api/v1/products/prod-2001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("prod-2001")
                .jsonPath("$.price.currency").isEqualTo("USD")
                .jsonPath("$.inventory.warehouseCode").isEqualTo("WH-01")
                .jsonPath("$.warnings").isArray();
    }

    @Test
    void getProductReturnsPartialAggregateWithWarningsWhenEnrichmentDownstreamIsDown() {
        ProductAggregate aggregate = new ProductAggregate(CATALOG, null, INVENTORY, List.of("pricing unavailable: boom"));
        given(aggregationService.getProduct(eq("prod-2001"), any())).willReturn(Mono.just(aggregate));

        webTestClient.get().uri("/api/v1/products/prod-2001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").isEqualTo(null)
                .jsonPath("$.warnings[0]").isEqualTo("pricing unavailable: boom");
    }

    @Test
    void getProductReturns404WhenProductMissing() {
        given(aggregationService.getProduct(eq("missing"), any()))
                .willReturn(Mono.error(new ProductNotFoundException("missing")));

        webTestClient.get().uri("/api/v1/products/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("NOT_FOUND");
    }

    @Test
    void listProductsReturnsAggregatedStream() {
        ProductAggregate aggregate = new ProductAggregate(CATALOG, PRICE, INVENTORY, List.of());
        given(aggregationService.listProducts(any())).willReturn(Flux.just(aggregate));

        webTestClient.get().uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void createRejectsInvalidPayload() {
        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.marcosperboni.integrationbff.web.dto.ProductCommandRequest("", "", "", null))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createReturns201() {
        given(commandService.create(any(), any())).willReturn(Mono.just(CATALOG));

        webTestClient.post().uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.marcosperboni.integrationbff.web.dto.ProductCommandRequest("Keyboard", "desc", "Electronics", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("prod-2001");
    }

    @Test
    void updateReturns200() {
        given(commandService.update(eq("prod-2001"), any(), any())).willReturn(Mono.just(CATALOG));

        webTestClient.put().uri("/api/v1/products/prod-2001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new com.marcosperboni.integrationbff.web.dto.ProductCommandRequest("Keyboard", "desc", "Electronics", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Keyboard");
    }

    @Test
    void deleteReturnsNoContent() {
        given(commandService.delete(eq("prod-2001"), any())).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/products/prod-2001")
                .exchange()
                .expectStatus().isNoContent();
    }
}
