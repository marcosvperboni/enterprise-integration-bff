package com.marcosperboni.pricingapi.domain;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(String productId) {
        super("Price not found for product: " + productId);
    }
}
