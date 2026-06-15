package com.sch.energyBrokerService.adapter.in.web.dto.Request;

import java.util.UUID;

import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Query sugli accordi con filtri opzionali", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class AgreementSearchQuery {

    @Schema(description = "UUID del CPO")
    private final UUID cpoId;

    @Schema(description = "UUID dell'eMSP")
    private final UUID emspId;

    @Schema(description = "Stato dell'accordo", example = "PROPOSED")
    private final AgreementStatus status;
}
