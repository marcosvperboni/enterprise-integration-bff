package com.marcosperboni.integrationbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marcosperboni.integrationbff.domain.model.CatalogInfo;

/** Mirrors internal-catalog-api-mock's clean, internal-standard payload shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogUpstreamDto(String id, String name, String description, String category, boolean active) {

    public CatalogInfo toCatalogInfo() {
        return new CatalogInfo(id, name, description, category, active);
    }

    /** internal-catalog-api-mock's write payload has no id field - it is assigned or resolved from the path. */
    public record WriteRequest(String name, String description, String category, boolean active) {

        public static WriteRequest fromCatalogInfo(CatalogInfo catalog) {
            return new WriteRequest(catalog.name(), catalog.description(), catalog.category(), catalog.active());
        }
    }
}
