package com.marcosperboni.pricingapi.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.pricingapi.application.PriceService;
import com.marcosperboni.pricingapi.domain.Price;
import com.marcosperboni.pricingapi.domain.PriceNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService service;

    @Test
    void findByProductIdReturnsPrice() throws Exception {
        given(service.findByProductId("prod-2001"))
                .willReturn(new Price("prod-2001", "USD", new BigDecimal("89.90"), 10));

        mockMvc.perform(get("/api/pricing/prod-2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("prod-2001"))
                .andExpect(jsonPath("$.amount").value(89.90))
                .andExpect(jsonPath("$.discountPercentage").value(10));
    }

    @Test
    void findByProductIdReturns404WhenMissing() throws Exception {
        given(service.findByProductId("missing")).willThrow(new PriceNotFoundException("missing"));

        mockMvc.perform(get("/api/pricing/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
