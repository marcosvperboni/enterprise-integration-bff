package com.marcosperboni.pricingapi.web.dto;

import com.marcosperboni.pricingapi.domain.Price;
import java.math.BigDecimal;

public record PriceResponse(String productId, String currency, BigDecimal amount, int discountPercentage) {

    public static PriceResponse from(Price price) {
        return new PriceResponse(price.productId(), price.currency(), price.amount(), price.discountPercentage());
    }
}
