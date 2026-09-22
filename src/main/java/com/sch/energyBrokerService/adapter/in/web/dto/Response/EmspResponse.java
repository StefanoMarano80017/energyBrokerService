package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con un eMSP")
public record EmspResponse(
        @Schema(description = "UUID eMSP, identificativo interno all'Energy Broker") UUID id,
        @Schema(description = "Identificativo assegnato dal sistema esterno (null se assente)",
                example = "EMSP-001")                                               String externalId,
        @Schema(description = "Nome eMSP")                                          String name
) {}
