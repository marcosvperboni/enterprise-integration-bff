package com.marcosperboni.integrationbff.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clients")
public record ClientsProperties(Client pricingService, Client inventoryService, Client catalogService) {

    public record Client(String baseUrl, long timeoutMillis) {
    }
}
