package com.marcosperboni.legacyinventory.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LegacyInventoryResponseTest {

    @Test
    void fromMapsCleanDomainRecordIntoQuirkyLegacyShape() {
        InventoryRecord record = new InventoryRecord("prod-2001", 120, "WH-01", LocalDate.of(2026, 9, 10));

        LegacyInventoryResponse response = LegacyInventoryResponse.from(record);

        assertThat(response.prodId()).isEqualTo("prod-2001");
        assertThat(response.qtyAvail()).isEqualTo("120");
        assertThat(response.whsCd()).isEqualTo("WH-01");
        assertThat(response.lastUpdDt()).isEqualTo("20260910");
    }
}
