package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema (description = "Richiesta di aggiornamento della tariffa di un accordo")
public record UpdateTariffRequest (
    @NotNull (message = "tariff is required")
    @Schema (description = "Nuova tariffa da applicare all'accordo")
    TariffPayload tariff
){}
