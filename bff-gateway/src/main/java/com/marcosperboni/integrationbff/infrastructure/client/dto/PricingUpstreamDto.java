package com.marcosperboni.integrationbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.marcosperboni.integrationbff.domain.model.PriceInfo;
import java.math.BigDecimal;

/** Mirrors external-pricing-api-mock's clean, modern JSON payload shape. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PricingUpstreamDto(String productId, String currency, BigDecimal amount, int discountPercentage) {

    public PriceInfo toPriceInfo() {
        return new PriceInfo(currency, amount, discountPercentage);
    }
}
