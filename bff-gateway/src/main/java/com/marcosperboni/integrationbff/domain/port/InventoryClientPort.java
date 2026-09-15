package com.marcosperboni.integrationbff.domain.port;

import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import reactor.core.publisher.Mono;

public interface InventoryClientPort {

    Mono<InventoryInfo> getInventory(String productId, String authorization);
}
