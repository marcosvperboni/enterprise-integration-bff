package com.marcosperboni.pricingapi.application;

import com.marcosperboni.pricingapi.domain.Price;
import com.marcosperboni.pricingapi.domain.PriceNotFoundException;
import com.marcosperboni.pricingapi.domain.PriceRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    private final PriceRepository repository;

    public PriceService(PriceRepository repository) {
        this.repository = repository;
    }

    public List<Price> findAll() {
        return repository.findAll();
    }

    public Price findByProductId(String productId) {
        return repository.findByProductId(productId).orElseThrow(() -> new PriceNotFoundException(productId));
    }
}
