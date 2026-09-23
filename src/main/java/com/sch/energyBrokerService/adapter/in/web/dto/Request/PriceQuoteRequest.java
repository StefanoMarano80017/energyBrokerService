package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Richiesta di preventivo: costo di una ricarica per ogni tariffa attiva di un CPO")
public record PriceQuoteRequest(

        @NotBlank(message = "Cpo name is required")
        @Schema(description = "Nome del CPO (non distingue maiuscole e minuscole)", example = "Charge Spa")
        String cpoName,

        @NotNull(message = "kWh is required")
        @Positive(message = "kWh must be positive")
        @Schema(description = "Energia richiesta in kWh", example = "30")
        BigDecimal kwh

) {}
