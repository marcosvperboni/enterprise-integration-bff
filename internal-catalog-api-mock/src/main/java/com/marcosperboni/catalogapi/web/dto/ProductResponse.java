package com.marcosperboni.catalogapi.web.dto;

import com.marcosperboni.catalogapi.domain.Product;
import java.time.Instant;

public record ProductResponse(
        String id,
        String name,
        String description,
        String category,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.name(), product.description(), product.category(),
                product.active(), product.createdAt(), product.updatedAt());
    }
}
