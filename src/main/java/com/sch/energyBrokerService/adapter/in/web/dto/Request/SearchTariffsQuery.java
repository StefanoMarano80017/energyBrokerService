package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.util.UUID;

import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Query sulle tariffe con filtri opzionali", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class SearchTariffsQuery {
    @Schema(description = "UUID fornitore")
    private final UUID supplierId;

    @Schema(description = "Latitudine del centro", example = "45.464211")
    private final Double latitude;
    
    @Schema(description = "Longitudine del centro", example = "9.191383")
    private final Double longitude;

    @Schema(description = "Raggio geografico in km", example = "10")
    private final Double radiusKm;

    @Schema(description = "Prezzo per kWh", example = "0.25")
    private final Double price;

    @Schema(description = "Tipo tariffa", example = "FLAT") 
    private final TariffType type;
}
