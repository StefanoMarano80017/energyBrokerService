package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Query sugli eMSP con filtri opzionali", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class EmspSearchQuery {

    @Schema(description = "filtro sul nome dell'eMSP", example = "Mobility Spa")
    private final String name;

    @Schema(description = "Identificativo assegnato dal sistema esterno", example = "EMSP-001")
    private final String externalId;
}
