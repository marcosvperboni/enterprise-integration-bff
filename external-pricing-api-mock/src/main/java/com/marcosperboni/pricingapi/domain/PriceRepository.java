package com.marcosperboni.pricingapi.domain;

import java.util.List;
import java.util.Optional;

public interface PriceRepository {

    List<Price> findAll();

    Optional<Price> findByProductId(String productId);
}
