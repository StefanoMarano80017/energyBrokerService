package com.sch.energyBrokerService.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.AgreementSearchQuery;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.UpdateTariffRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.AgreementResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.command.PublishAgreementCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agreements")
@RequiredArgsConstructor
@Tag(name = "Agreement", description = "Accordi commerciali tra CPO ed eMSP")
public class AgreementController {

    private final PublishAgreementCommandHandler publishHandler;
    private final AgreementLifecycleCommandHandler lifecycleHandler;
    private final AgreementQueryHandler queryHandler;

    /**
    *  =========================================================
    *  POST ENDPOINTS
    *  =========================================================
    */
    @Operation(summary = "Pubblica un accordo: un eMSP propone una tariffa a un CPO (stato PROPOSED)")
    @PostMapping
    public AgreementResponse publish(@Valid @RequestBody CreateAgreementRequest request) {
        return AgreementPersistenceMapper.toResponse(publishHandler.handle(request));
    }

    @Operation(summary = "L'CPO accetta l'accordo (stato ACCEPTED)")
    @PostMapping("/{id}/accept")
    public AgreementResponse accept(@PathVariable UUID id) {
        return AgreementPersistenceMapper.toResponse(lifecycleHandler.accept(id));
    }

    @Operation(summary = "L'CPO rifiuta l'accordo (stato REJECTED)")
    @PostMapping("/{id}/reject")
    public AgreementResponse reject(@PathVariable UUID id) {
        return AgreementPersistenceMapper.toResponse(lifecycleHandler.reject(id));
    }

    @Operation(summary = "CPO Annulla l'accordo (stato CANCELLED)")
    @PostMapping("/{id}/cancel")
    public AgreementResponse cancel(@PathVariable UUID id) {
        return AgreementPersistenceMapper.toResponse(lifecycleHandler.cancel(id));
    }

    @Operation (summary = "CPO Aggiorna la tariffa dell'accordo")
    @PostMapping("/{id}/update-tariff")
    public AgreementResponse updateTariff(@PathVariable UUID id, @RequestBody @Valid UpdateTariffRequest request) {
        return AgreementPersistenceMapper.toResponse(lifecycleHandler.updateTariff(
            id, request.tariff().type(), request.tariff().pricePerKwh())
        );
    }

    /**
    *  =========================================================
    *  GET ENDPOINTS
    *  =========================================================
    */
    @Operation(summary = "Cerca gli accordi per CPO, eMSP e/o stato")
    @GetMapping
    public List<AgreementResponse> search(@ModelAttribute AgreementSearchQuery query) {
        return queryHandler
                .search(query.getCpoId(), query.getEmspId(), query.getStatus())
                .stream()
                .map(AgreementPersistenceMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Recupera un accordo per id")
    @GetMapping("/{id}")
    public AgreementResponse getById(@PathVariable UUID id) {
        return queryHandler
                .findById(id)
                .map(AgreementPersistenceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Agreement not found: " + id));
    }
}
