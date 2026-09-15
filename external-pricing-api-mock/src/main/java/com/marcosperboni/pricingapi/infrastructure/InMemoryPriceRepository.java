package com.marcosperboni.pricingapi.infrastructure;

import com.marcosperboni.pricingapi.domain.Price;
import com.marcosperboni.pricingapi.domain.PriceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for the third-party pricing platform. This mock
 * exists to demonstrate the BFF orchestration pattern, not persistence.
 */
@Repository
public class InMemoryPriceRepository implements PriceRepository {

    private final Map<String, Price> store = new ConcurrentHashMap<>();

    public InMemoryPriceRepository() {
        seed();
    }

    @Override
    public List<Price> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Price> findByProductId(String productId) {
        return Optional.ofNullable(store.get(productId));
    }

    private void seed() {
        store.put("prod-2001", new Price("prod-2001", "USD", new BigDecimal("89.90"), 10));
        store.put("prod-2002", new Price("prod-2002", "USD", new BigDecimal("249.00"), 0));
    }
}
