package com.marcosperboni.integrationbff.domain.port;

import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import reactor.core.publisher.Mono;

public interface PricingClientPort {

    Mono<PriceInfo> getPrice(String productId, String authorization);
}
