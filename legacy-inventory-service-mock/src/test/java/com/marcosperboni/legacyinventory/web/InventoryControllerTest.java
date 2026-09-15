package com.marcosperboni.legacyinventory.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.legacyinventory.application.InventoryService;
import com.marcosperboni.legacyinventory.domain.InventoryNotFoundException;
import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService service;

    @Test
    void findByProductIdReturnsQuirkyShape() throws Exception {
        given(service.findByProductId("prod-2001"))
                .willReturn(new InventoryRecord("prod-2001", 120, "WH-01", LocalDate.of(2026, 9, 10)));

        mockMvc.perform(get("/legacy/inventory/prod-2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROD_ID").value("prod-2001"))
                .andExpect(jsonPath("$.QTY_AVAIL").value("120"))
                .andExpect(jsonPath("$.WHS_CD").value("WH-01"))
                .andExpect(jsonPath("$.LAST_UPD_DT").value("20260910"));
    }

    @Test
    void findByProductIdReturns404WhenMissing() throws Exception {
        given(service.findByProductId("missing")).willThrow(new InventoryNotFoundException("missing"));

        mockMvc.perform(get("/legacy/inventory/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
