package com.marcosperboni.integrationbff.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(long productTtlSeconds) {
}
