package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Richiesta di creazione di un CPO")
public record CreateCpoRequest(

        @NotBlank(message = "Cpo name is required")
        @Schema(description = "Nome del CPO", example = "Charge Spa")
        String name,

        @NotNull(message = "Cpo latitude is required")
        @Schema(description = "Latitudine della posizione", example = "45.464211")
        Double latitude,

        @NotNull(message = "Cpo longitude is required")
        @Schema(description = "Longitudine della posizione", example = "9.191383")
        Double longitude

) {}
