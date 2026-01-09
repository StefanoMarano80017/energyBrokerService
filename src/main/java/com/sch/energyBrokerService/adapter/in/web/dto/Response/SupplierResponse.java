package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con un fornitore")
public record SupplierResponse(
        @Schema(description = "UUID fornitore") UUID id,
        @Schema(description = "Nome fornitore") String name
) {}