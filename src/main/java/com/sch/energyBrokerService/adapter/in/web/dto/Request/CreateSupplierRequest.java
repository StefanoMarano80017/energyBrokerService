package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Query sui fornitori con filtri opzionali")
public record CreateSupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Schema(description = "filtro sul nome del fornitore", example = "Energia Spa")
        String name

) {}