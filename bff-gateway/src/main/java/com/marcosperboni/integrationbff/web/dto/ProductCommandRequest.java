package com.marcosperboni.integrationbff.web.dto;

import com.marcosperboni.integrationbff.domain.model.CatalogInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCommandRequest(
        @NotBlank(message = "name is required") @Size(max = 120) String name,
        @NotBlank(message = "description is required") @Size(max = 500) String description,
        @NotBlank(message = "category is required") String category,
        @NotNull(message = "active is required") Boolean active) {

    public CatalogInfo toCatalogInfo(String productId) {
        return new CatalogInfo(productId, name, description, category, active);
    }
}
