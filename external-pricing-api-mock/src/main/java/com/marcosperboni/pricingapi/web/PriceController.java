package com.marcosperboni.pricingapi.web;

import com.marcosperboni.pricingapi.application.PriceService;
import com.marcosperboni.pricingapi.web.dto.PriceResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PriceController {

    private final PriceService service;

    public PriceController(PriceService service) {
        this.service = service;
    }

    @GetMapping
    public List<PriceResponse> findAll() {
        return service.findAll().stream().map(PriceResponse::from).toList();
    }

    @GetMapping("/{productId}")
    public PriceResponse findByProductId(@PathVariable String productId) {
        return PriceResponse.from(service.findByProductId(productId));
    }
}
