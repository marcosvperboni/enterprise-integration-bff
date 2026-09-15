package com.marcosperboni.catalogapi.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.catalogapi.domain.Product;
import com.marcosperboni.catalogapi.domain.ProductNotFoundException;
import com.marcosperboni.catalogapi.domain.ProductRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private final Map<String, Product> store = new HashMap<>();
    private ProductService service;

    @BeforeEach
    void setUp() {
        store.clear();
        service = new ProductService(new ProductRepository() {
            @Override
            public List<Product> findAll() {
                return List.copyOf(store.values());
            }

            @Override
            public Optional<Product> findById(String id) {
                return Optional.ofNullable(store.get(id));
            }

            @Override
            public Product save(Product product) {
                store.put(product.id(), product);
                return product;
            }

            @Override
            public boolean deleteById(String id) {
                return store.remove(id) != null;
            }
        });
    }

    @Test
    void createAssignsGeneratedId() {
        Product created = service.create("Desk Lamp", "LED desk lamp", "Home", true);

        assertThat(created.id()).startsWith("prod-");
        assertThat(service.findById(created.id())).isEqualTo(created);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> service.findById("missing")).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updatePreservesIdAndCreatedAt() {
        Instant now = Instant.now();
        store.put("prod-1", new Product("prod-1", "Old", "Old desc", "Cat", true, now, now));

        Product updated = service.update("prod-1", "New", "New desc", "Cat2", false);

        assertThat(updated.id()).isEqualTo("prod-1");
        assertThat(updated.createdAt()).isEqualTo(now);
        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.active()).isFalse();
    }

    @Test
    void deleteThrowsWhenMissing() {
        assertThatThrownBy(() -> service.delete("missing")).isInstanceOf(ProductNotFoundException.class);
    }
}
