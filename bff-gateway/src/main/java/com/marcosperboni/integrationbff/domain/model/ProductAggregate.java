package com.marcosperboni.integrationbff.domain.model;

import java.util.List;

/**
 * Unified view merging the internal catalog (mandatory - a product with no
 * identity is meaningless), external pricing and legacy inventory. Pricing
 * and inventory degrade to null plus a warning when their downstream is
 * unavailable, so a partial outage never turns into a hard error for the
 * client.
 */
public record ProductAggregate(CatalogInfo catalog, PriceInfo price, InventoryInfo inventory, List<String> warnings) {
}
