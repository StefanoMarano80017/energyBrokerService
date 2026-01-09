package com.sch.energyBrokerService.adapter.in.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateTariffRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.SearchTariffsQuery;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.TariffPersistenceMapper;
import com.sch.energyBrokerService.application.service.tariff.command.CreateTariffCommandHandler;
import com.sch.energyBrokerService.application.service.tariff.query.TariffQueryHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
@Tag(
    name = "Tariff", 
    description = "Gestione tariffe, ogni tariffa è associata a un fornitore e a una copertura geografica."
)
public class TariffController {

    private final CreateTariffCommandHandler commandHandler;
    private final TariffQueryHandler queryHandler;

    @Operation(summary = "Crea una nuova tariffa")
    @PostMapping
    public TariffResponse createTariff(@Valid @RequestBody CreateTariffRequest request) {
        return commandHandler.handle(request);
    }

    @Operation(summary = "Cerca tariffe con filtri e paginazione")
    @GetMapping
    public Page<TariffResponse> searchTariffs(
        @ModelAttribute SearchTariffsQuery query,  
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return queryHandler.searchTariffs(
                query.getSupplierId(),
                query.getLatitude(),
                query.getLongitude(),
                query.getRadiusKm(),
                query.getPrice(),
                query.getType(),
                pageable
        ).map(TariffPersistenceMapper::toResponse);
    }

    @Operation(summary = "Verifica se un punto è coperto da tariffe con filtri e paginazione")
    @GetMapping("/coverage")
    public Page<TariffResponse> getCoverage(
        @ModelAttribute SearchTariffsQuery query,  
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return queryHandler.findCoveringTariffs(
                query.getSupplierId(),
                query.getLatitude(),
                query.getLongitude(),
                pageable
        ).map(TariffPersistenceMapper::toResponse);
    }

}
