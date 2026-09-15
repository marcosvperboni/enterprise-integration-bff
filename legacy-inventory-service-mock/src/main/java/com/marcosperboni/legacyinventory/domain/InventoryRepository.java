package com.marcosperboni.legacyinventory.domain;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {

    List<InventoryRecord> findAll();

    Optional<InventoryRecord> findByProductId(String productId);
}
