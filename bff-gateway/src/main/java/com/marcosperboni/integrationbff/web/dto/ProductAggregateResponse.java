package com.marcosperboni.integrationbff.web.dto;

import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import com.marcosperboni.integrationbff.domain.model.ProductAggregate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductAggregateResponse(
        String id,
        String name,
        String description,
        String category,
        boolean active,
        PriceDetails price,
        InventoryDetails inventory,
        List<String> warnings) {

    public static ProductAggregateResponse from(ProductAggregate aggregate) {
        return new ProductAggregateResponse(
                aggregate.catalog().productId(),
                aggregate.catalog().name(),
                aggregate.catalog().description(),
                aggregate.catalog().category(),
                aggregate.catalog().active(),
                PriceDetails.from(aggregate.price()),
                InventoryDetails.from(aggregate.inventory()),
                aggregate.warnings());
    }

    public record PriceDetails(String currency, BigDecimal amount, int discountPercentage) {

        static PriceDetails from(PriceInfo price) {
            return price == null ? null : new PriceDetails(price.currency(), price.amount(), price.discountPercentage());
        }
    }

    public record InventoryDetails(int quantityAvailable, String warehouseCode, LocalDate lastUpdated) {

        static InventoryDetails from(InventoryInfo inventory) {
            return inventory == null ? null
                    : new InventoryDetails(inventory.quantityAvailable(), inventory.warehouseCode(), inventory.lastUpdated());
        }
    }
}
