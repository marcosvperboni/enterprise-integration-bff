package com.marcosperboni.integrationbff.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.marcosperboni.integrationbff.domain.model.InventoryInfo;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Mirrors legacy-inventory-service-mock's quirky wire shape - abbreviated
 * uppercase field names, string-typed quantity, string-typed yyyyMMdd date.
 * This is the normalization boundary the architecture diagram labels
 * "Normalizacao dos dados": every translation from the legacy shape into the
 * clean domain model {@link InventoryInfo} happens here, once, so the rest of
 * the BFF never has to know the legacy system exists.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LegacyInventoryUpstreamDto(
        @JsonProperty("PROD_ID") String prodId,
        @JsonProperty("QTY_AVAIL") String qtyAvail,
        @JsonProperty("WHS_CD") String whsCd,
        @JsonProperty("LAST_UPD_DT") String lastUpdDt) {

    private static final DateTimeFormatter LEGACY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public InventoryInfo toInventoryInfo() {
        return new InventoryInfo(Integer.parseInt(qtyAvail), whsCd, LocalDate.parse(lastUpdDt, LEGACY_DATE_FORMAT));
    }
}
