package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con un eMSP")
public record EmspResponse(
        @Schema(description = "UUID eMSP") UUID id,
        @Schema(description = "Nome eMSP") String name
) {}
