package com.marcosperboni.legacyinventory.domain;

import java.time.LocalDate;

/** Modeled in clean domain shape; the quirky legacy wire format lives only in the web layer. */
public record InventoryRecord(String productId, int quantityAvailable, String warehouseCode, LocalDate lastUpdated) {
}
