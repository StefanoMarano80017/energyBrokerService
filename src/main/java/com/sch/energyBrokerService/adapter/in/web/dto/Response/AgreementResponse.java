package com.sch.energyBrokerService.adapter.in.web.dto.Response;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risposta con un accordo commerciale CPO-eMSP")
public record AgreementResponse(
        @Schema(description = "UUID accordo")                                  UUID id,
        @Schema(description = "UUID CPO proponente")                           UUID cpoId,
        @Schema(description = "UUID eMSP destinatario")                        UUID emspId,
        @Schema(description = "Tariffa oggetto dell'accordo")                  TariffView tariff,
        @Schema(description = "Stato", example = "PROPOSED")                   String status,
        @Schema(description = "Data di creazione")                            Instant createdAt,
        @Schema(description = "Data di accettazione (null se non accettato)") Instant acceptedAt
) {}
