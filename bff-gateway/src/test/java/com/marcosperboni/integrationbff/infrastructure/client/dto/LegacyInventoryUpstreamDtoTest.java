package com.marcosperboni.integrationbff.infrastructure.client.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Normalization is the piece the architecture diagram calls out as the
 * BFF's real work - these tests pin down the exact translation from the
 * legacy system's quirky wire shape into the clean domain model.
 */
class LegacyInventoryUpstreamDtoTest {

    @Test
    void normalizesAbbreviatedFieldsStringQuantityAndYyyyMMddDateIntoCleanDomainModel() {
        LegacyInventoryUpstreamDto quirky = new LegacyInventoryUpstreamDto("prod-2001", "120", "WH-01", "20260910");

        InventoryInfo normalized = quirky.toInventoryInfo();

        assertThat(normalized).isEqualTo(new InventoryInfo(120, "WH-01", LocalDate.of(2026, 9, 10)));
    }

    @Test
    void normalizesZeroQuantityCorrectly() {
        LegacyInventoryUpstreamDto quirky = new LegacyInventoryUpstreamDto("prod-2002", "0", "WH-02", "20260101");

        assertThat(quirky.toInventoryInfo().quantityAvailable()).isZero();
    }

    @Test
    void throwsOnMalformedQuantity() {
        LegacyInventoryUpstreamDto malformed = new LegacyInventoryUpstreamDto("prod-2001", "not-a-number", "WH-01", "20260910");

        assertThatThrownBy(malformed::toInventoryInfo).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void throwsOnMalformedDate() {
        LegacyInventoryUpstreamDto malformed = new LegacyInventoryUpstreamDto("prod-2001", "120", "WH-01", "2026-09-10");

        assertThatThrownBy(malformed::toInventoryInfo).isInstanceOf(java.time.format.DateTimeParseException.class);
    }
}
