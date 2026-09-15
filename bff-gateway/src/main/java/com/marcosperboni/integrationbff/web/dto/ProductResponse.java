package com.marcosperboni.integrationbff.web.dto;

import com.marcosperboni.integrationbff.domain.model.CatalogInfo;

public record ProductResponse(String id, String name, String description, String category, boolean active) {

    public static ProductResponse from(CatalogInfo catalog) {
        return new ProductResponse(catalog.productId(), catalog.name(), catalog.description(), catalog.category(),
                catalog.active());
    }
}
