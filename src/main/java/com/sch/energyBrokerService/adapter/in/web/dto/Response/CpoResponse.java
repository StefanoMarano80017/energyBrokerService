package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con un CPO")
public record CpoResponse(
        @Schema(description = "UUID CPO, identificativo interno all'Energy Broker") UUID id,
        @Schema(description = "Identificativo assegnato dal sistema esterno (null se assente)",
                example = "CPO-001")                                              String externalId,
        @Schema(description = "Nome CPO")                                         String name,
        @Schema(description = "Latitudine della posizione", example = "45.464211") Double latitude,
        @Schema(description = "Longitudine della posizione", example = "9.191383") Double longitude
) {}
