package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Richiesta di creazione di un eMSP")
public record CreateEmspRequest(

        @NotBlank(message = "Emsp name is required")
        @Schema(description = "Nome dell'eMSP", example = "Mobility Spa")
        String name

) {}
