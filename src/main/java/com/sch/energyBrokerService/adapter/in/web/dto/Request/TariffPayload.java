package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.math.BigDecimal;

import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Definizione economica della tariffa proposta nell'accordo")
public record TariffPayload(

        @NotNull(message = "Tariff type is required")
        @Schema(description = "Tipo tariffa", example = "FLAT")
        TariffType type,

        @NotNull(message = "Tariff pricePerKwh is required")
        @Positive(message = "Tariff pricePerKwh must be positive")
        @Schema(description = "Prezzo per kWh", example = "0.25")
        BigDecimal pricePerKwh

) {}
