package com.marcosperboni.catalogapi.infrastructure;

import com.marcosperboni.catalogapi.domain.Product;
import com.marcosperboni.catalogapi.domain.ProductRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for a real persistence layer. This service exists to
 * demonstrate the BFF orchestration pattern, not database design, so a
 * ConcurrentHashMap is enough - swap for a JPA/R2DBC repository if this ever
 * needs to survive a restart.
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new ConcurrentHashMap<>();

    public InMemoryProductRepository() {
        seed();
    }

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

    private void seed() {
        Instant now = Instant.now();
        save(new Product("prod-2001", "Wireless Mechanical Keyboard",
                "Hot-swappable mechanical keyboard with per-key RGB", "Electronics", true, now, now));
        save(new Product("prod-2002", "Ergonomic Office Chair",
                "Mesh-back office chair with adjustable lumbar support", "Furniture", true, now, now));
    }
}
