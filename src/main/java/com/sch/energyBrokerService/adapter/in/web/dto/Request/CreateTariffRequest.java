package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.math.BigDecimal;
import java.util.UUID;

import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTariffRequest (

    @NotNull(message = "Tariff supplier ID is required")
    UUID supplierId,

    @NotNull(message = "Tariff type is required")
    TariffType type,

    @NotNull(message = "Tariff pricePerKwh is required")
    @Positive(message = "Tariff pricePerKwh must be positive")
    BigDecimal pricePerKwh,

    @NotNull(message = "Tariff latitude is required")
    Double latitude,

    @NotNull(message = "Tariff longitude is required")
    Double longitude,

    @NotNull(message = "Tariff geo radius is required")
    @Positive(message = "Tariff geo radius must be positive")
    Double geoRadiusKm

) {}