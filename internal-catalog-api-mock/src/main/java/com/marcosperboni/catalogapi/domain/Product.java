package com.marcosperboni.catalogapi.domain;

import java.time.Instant;

public record Product(
        String id,
        String name,
        String description,
        String category,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public Product withUpdatedFields(String name, String description, String category, boolean active) {
        return new Product(this.id, name, description, category, active, this.createdAt, Instant.now());
    }
}
