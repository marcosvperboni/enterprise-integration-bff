package com.marcosperboni.legacyinventory.infrastructure;

import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import com.marcosperboni.legacyinventory.domain.InventoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory store standing in for the legacy mainframe-backed inventory
 * system. This mock exists to demonstrate the BFF normalization pattern, not
 * persistence.
 */
@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, InventoryRecord> store = new ConcurrentHashMap<>();

    public InMemoryInventoryRepository() {
        seed();
    }

    @Override
    public List<InventoryRecord> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<InventoryRecord> findByProductId(String productId) {
        return Optional.ofNullable(store.get(productId));
    }

    private void seed() {
        store.put("prod-2001", new InventoryRecord("prod-2001", 120, "WH-01", LocalDate.of(2026, 9, 10)));
        store.put("prod-2002", new InventoryRecord("prod-2002", 0, "WH-02", LocalDate.of(2026, 9, 12)));
    }
}
