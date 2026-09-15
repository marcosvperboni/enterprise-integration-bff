package com.marcosperboni.integrationbff.infrastructure.cache;

import com.marcosperboni.integrationbff.config.properties.CacheProperties;
import com.marcosperboni.integrationbff.domain.model.ProductAggregate;
import java.time.Duration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Caches the aggregated product view in Redis so a burst of requests for the
 * same product doesn't hammer three downstream systems. Serialized as JSON
 * strings via the app's own Jackson ObjectMapper - avoids pulling in a
 * separate Jackson2-based Redis serializer just for this.
 */
@Component
public class ProductAggregateCacheService {

    private static final String KEY_PREFIX = "product:aggregate:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public ProductAggregateCacheService(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            CacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(cacheProperties.productTtlSeconds());
    }

    public Mono<ProductAggregate> get(String productId) {
        return redisTemplate.opsForValue().get(key(productId))
                .map(json -> objectMapper.readValue(json, ProductAggregate.class))
                .onErrorResume(ex -> Mono.empty());
    }

    public Mono<Boolean> put(String productId, ProductAggregate aggregate) {
        String json = objectMapper.writeValueAsString(aggregate);
        return redisTemplate.opsForValue().set(key(productId), json, ttl)
                .onErrorResume(ex -> Mono.just(false));
    }

    public Mono<Boolean> evict(String productId) {
        return redisTemplate.opsForValue().delete(key(productId))
                .onErrorResume(ex -> Mono.just(false));
    }

    private String key(String productId) {
        return KEY_PREFIX + productId;
    }
}
