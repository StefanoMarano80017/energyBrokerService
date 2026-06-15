package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Definizione economica della tariffa oggetto dell'accordo")
public record TariffView(
        @Schema(description = "UUID tariffa")                       UUID tariffId,
        @Schema(description = "Tipo tariffa", example = "FLAT")     String type,
        @Schema(description = "Prezzo per kWh", example = "0.25")   BigDecimal pricePerKwh
) {}
