package com.marcosperboni.catalogapi.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marcosperboni.catalogapi.application.ProductService;
import com.marcosperboni.catalogapi.domain.Product;
import com.marcosperboni.catalogapi.domain.ProductNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    private static final Product PRODUCT = new Product("prod-2001", "Keyboard", "desc", "Electronics", true,
            Instant.now(), Instant.now());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void findByIdReturnsProduct() throws Exception {
        given(service.findById("prod-2001")).willReturn(PRODUCT);

        mockMvc.perform(get("/api/catalog/products/prod-2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-2001"))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        given(service.findById("missing")).willThrow(new ProductNotFoundException("missing"));

        mockMvc.perform(get("/api/catalog/products/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void createRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"\",\"category\":\"\",\"active\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void createReturns201() throws Exception {
        given(service.create(eq("Keyboard"), any(), any(), eq(true))).willReturn(PRODUCT);

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Keyboard\",\"description\":\"desc\",\"category\":\"Electronics\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("prod-2001"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/catalog/products/prod-2001"))
                .andExpect(status().isNoContent());
    }
}
