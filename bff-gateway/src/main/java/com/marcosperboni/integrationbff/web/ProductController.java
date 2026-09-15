package com.marcosperboni.integrationbff.web;

import com.marcosperboni.integrationbff.application.ProductAggregationService;
import com.marcosperboni.integrationbff.application.ProductCommandService;
import com.marcosperboni.integrationbff.web.dto.ProductAggregateResponse;
import com.marcosperboni.integrationbff.web.dto.ProductCommandRequest;
import com.marcosperboni.integrationbff.web.dto.ProductResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductAggregationService aggregationService;
    private final ProductCommandService commandService;

    public ProductController(ProductAggregationService aggregationService, ProductCommandService commandService) {
        this.aggregationService = aggregationService;
        this.commandService = commandService;
    }

    @GetMapping
    public Flux<ProductAggregateResponse> listProducts(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return aggregationService.listProducts(authorization).map(ProductAggregateResponse::from);
    }

    @GetMapping("/{productId}")
    public Mono<ProductAggregateResponse> getProduct(@PathVariable String productId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return aggregationService.getProduct(productId, authorization).map(ProductAggregateResponse::from);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductResponse>> create(@Valid @RequestBody ProductCommandRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.create(request.toCatalogInfo(null), authorization)
                .map(created -> ResponseEntity.created(URI.create("/api/v1/products/" + created.productId()))
                        .body(ProductResponse.from(created)));
    }

    @PutMapping("/{productId}")
    public Mono<ProductResponse> update(@PathVariable String productId,
            @Valid @RequestBody ProductCommandRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.update(productId, request.toCatalogInfo(productId), authorization)
                .map(ProductResponse::from);
    }

    @DeleteMapping("/{productId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String productId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.delete(productId, authorization).thenReturn(ResponseEntity.noContent().build());
    }
}
