package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Query sui fornitori con filtri opzionali", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class SupplierSearchQuery {

    @Schema(description = "filtro sul nome del fornitore", example = "Energia Spa")
    private final String name;
}
