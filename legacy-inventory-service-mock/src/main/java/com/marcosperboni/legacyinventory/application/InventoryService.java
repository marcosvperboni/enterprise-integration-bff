package com.marcosperboni.legacyinventory.application;

import com.marcosperboni.legacyinventory.domain.InventoryNotFoundException;
import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import com.marcosperboni.legacyinventory.domain.InventoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public List<InventoryRecord> findAll() {
        return repository.findAll();
    }

    public InventoryRecord findByProductId(String productId) {
        return repository.findByProductId(productId).orElseThrow(() -> new InventoryNotFoundException(productId));
    }
}
