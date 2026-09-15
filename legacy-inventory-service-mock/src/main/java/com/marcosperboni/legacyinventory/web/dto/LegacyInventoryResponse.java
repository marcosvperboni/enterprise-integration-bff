package com.marcosperboni.legacyinventory.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marcosperboni.legacyinventory.domain.InventoryRecord;
import java.time.format.DateTimeFormatter;

/**
 * Deliberately quirky wire shape - abbreviated uppercase field names,
 * string-typed quantity, string-typed date in yyyyMMdd - mirroring the kind
 * of payload a decades-old mainframe-backed inventory system would emit.
 * Normalizing this into a clean shape is the consuming BFF's job, not this
 * mock's.
 */
public record LegacyInventoryResponse(
        @JsonProperty("PROD_ID") String prodId,
        @JsonProperty("QTY_AVAIL") String qtyAvail,
        @JsonProperty("WHS_CD") String whsCd,
        @JsonProperty("LAST_UPD_DT") String lastUpdDt) {

    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static LegacyInventoryResponse from(InventoryRecord record) {
        return new LegacyInventoryResponse(
                record.productId(),
                String.valueOf(record.quantityAvailable()),
                record.warehouseCode(),
                record.lastUpdated().format(LEGACY_DATE_FORMAT));
    }
}
