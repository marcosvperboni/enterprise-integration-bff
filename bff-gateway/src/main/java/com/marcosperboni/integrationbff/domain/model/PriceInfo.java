package com.marcosperboni.integrationbff.domain.model;

import java.math.BigDecimal;

public record PriceInfo(String currency, BigDecimal amount, int discountPercentage) {
}
