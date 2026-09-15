package com.marcosperboni.pricingapi.domain;

import java.math.BigDecimal;

public record Price(String productId, String currency, BigDecimal amount, int discountPercentage) {
}
