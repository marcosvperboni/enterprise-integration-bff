package com.marcosperboni.integrationbff.domain.model;

import java.time.LocalDate;

public record InventoryInfo(int quantityAvailable, String warehouseCode, LocalDate lastUpdated) {
}
