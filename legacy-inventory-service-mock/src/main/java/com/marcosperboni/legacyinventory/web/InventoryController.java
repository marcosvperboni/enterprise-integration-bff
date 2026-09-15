package com.marcosperboni.legacyinventory.web;

import com.marcosperboni.legacyinventory.application.InventoryService;
import com.marcosperboni.legacyinventory.web.dto.LegacyInventoryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Path deliberately mirrors legacy-system naming conventions, not the modern /api/v1 style. */
@RestController
@RequestMapping("/legacy/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LegacyInventoryResponse> findAll() {
        return service.findAll().stream().map(LegacyInventoryResponse::from).toList();
    }

    @GetMapping("/{productId}")
    public LegacyInventoryResponse findByProductId(@PathVariable String productId) {
        return LegacyInventoryResponse.from(service.findByProductId(productId));
    }
}
