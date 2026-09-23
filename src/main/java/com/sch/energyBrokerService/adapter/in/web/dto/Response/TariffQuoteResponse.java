package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Costo di una ricarica calcolato su una tariffa di un accordo attivo")
public record TariffQuoteResponse(
        @Schema(description = "UUID accordo")                            UUID agreementId,
        @Schema(description = "UUID CPO")                                UUID cpoId,
        @Schema(description = "UUID eMSP")                               UUID emspId,
        @Schema(description = "UUID tariffa")                            UUID tariffId,
        @Schema(description = "Tipo tariffa", example = "FLAT")          String type,
        @Schema(description = "Prezzo per kWh", example = "0.2500")      BigDecimal pricePerKwh,
        @Schema(description = "Energia richiesta in kWh", example = "30") BigDecimal kwh,
        @Schema(description = "Costo totale", example = "7.50")          BigDecimal totalCost
) {}
