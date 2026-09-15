package com.marcosperboni.catalogapi.application;

import com.marcosperboni.catalogapi.domain.Product;
import com.marcosperboni.catalogapi.domain.ProductNotFoundException;
import com.marcosperboni.catalogapi.domain.ProductRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(String name, String description, String category, boolean active) {
        Instant now = Instant.now();
        Product product = new Product("prod-" + UUID.randomUUID(), name, description, category, active, now, now);
        return repository.save(product);
    }

    public Product update(String id, String name, String description, String category, boolean active) {
        Product existing = findById(id);
        return repository.save(existing.withUpdatedFields(name, description, category, active));
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new ProductNotFoundException(id);
        }
    }
}
