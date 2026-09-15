package com.marcosperboni.legacyinventory.domain;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String productId) {
        super("Inventory record not found for product: " + productId);
    }
}
