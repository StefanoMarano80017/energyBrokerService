package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con una tariffa")
public record TariffResponse(
    @Schema(description = "UUID tariffa")                                 UUID tariffId,
    @Schema(description = "Nome Fornitore")                               String supplierName,   
    @Schema(description = "UUID Fornitore")                               String supplierId,
    @Schema(description = "Tipo tariffa", example = "FLAT")               String type,
    @Schema(description = "Prezzo per kWh", example = "0.25")             BigDecimal amount,
    @Schema(description = "Latitudine del centro", example = "45.464211") Double latitude,
    @Schema(description = "Longitudine del centro", example = "9.191383") Double longitude,
    @Schema(description = "Raggio geografico in km", example = "10")      Double geoRadiusKm
){}