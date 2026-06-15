package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Richiesta di pubblicazione di un accordo: un CPO propone una tariffa a un eMSP")
public record CreateAgreementRequest(

        @NotNull(message = "cpoId is required")
        @Schema(description = "UUID del CPO proponente")
        UUID cpoId,

        @NotNull(message = "emspId is required")
        @Schema(description = "UUID dell'eMSP destinatario")
        UUID emspId,

        @NotNull(message = "tariff is required")
        @Valid
        @Schema(description = "Tariffa oggetto dell'accordo")
        TariffPayload tariff

) {}
