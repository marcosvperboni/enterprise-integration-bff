package com.marcosperboni.legacyinventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.legacyinventory.domain.InventoryNotFoundException;
import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import com.marcosperboni.legacyinventory.domain.InventoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    private static final InventoryRecord RECORD = new InventoryRecord("prod-2001", 120, "WH-01", LocalDate.of(2026, 9, 10));

    private final InventoryService service = new InventoryService(new InventoryRepository() {
        @Override
        public List<InventoryRecord> findAll() {
            return List.of(RECORD);
        }

        @Override
        public Optional<InventoryRecord> findByProductId(String productId) {
            return productId.equals("prod-2001") ? Optional.of(RECORD) : Optional.empty();
        }
    });

    @Test
    void findByProductIdReturnsRecord() {
        assertThat(service.findByProductId("prod-2001")).isEqualTo(RECORD);
    }

    @Test
    void findByProductIdThrowsWhenMissing() {
        assertThatThrownBy(() -> service.findByProductId("missing")).isInstanceOf(InventoryNotFoundException.class);
    }
}
