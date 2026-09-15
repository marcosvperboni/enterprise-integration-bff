package com.marcosperboni.pricingapi.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.pricingapi.domain.Price;
import com.marcosperboni.pricingapi.domain.PriceNotFoundException;
import com.marcosperboni.pricingapi.domain.PriceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PriceServiceTest {

    private static final Price PRICE = new Price("prod-2001", "USD", new BigDecimal("89.90"), 10);

    private final PriceService service = new PriceService(new PriceRepository() {
        @Override
        public List<Price> findAll() {
            return List.of(PRICE);
        }

        @Override
        public Optional<Price> findByProductId(String productId) {
            return productId.equals("prod-2001") ? Optional.of(PRICE) : Optional.empty();
        }
    });

    @Test
    void findByProductIdReturnsPrice() {
        assertThat(service.findByProductId("prod-2001")).isEqualTo(PRICE);
    }

    @Test
    void findByProductIdThrowsWhenMissing() {
        assertThatThrownBy(() -> service.findByProductId("missing")).isInstanceOf(PriceNotFoundException.class);
    }

    @Test
    void findAllReturnsSeededPrices() {
        assertThat(service.findAll()).containsExactly(PRICE);
    }
}
