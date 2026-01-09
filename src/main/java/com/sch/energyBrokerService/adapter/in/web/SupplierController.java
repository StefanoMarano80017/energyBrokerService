package com.sch.energyBrokerService.adapter.in.web;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateSupplierRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.SupplierSearchQuery;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.SupplierResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.SupplierPersistenceMapper;
import com.sch.energyBrokerService.application.service.supplier.command.SupplierCommandHandler;
import com.sch.energyBrokerService.application.service.supplier.query.SupplierQueryHandler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier", description = "Gestione fornitori di energia")
public class SupplierController {

    private final SupplierCommandHandler commandHandler;
    private final SupplierQueryHandler queryHandler;

    @Operation(summary = "Crea un nuovo fornitore")
    @PostMapping
    public SupplierResponse create(
        @Valid @RequestBody CreateSupplierRequest request
    ) {
        return commandHandler.create(request);
    }

    @Operation(summary = "Cerca fornitori con filtri e paginazione")
    @GetMapping
    public Page<SupplierResponse> search(
        @Valid @ModelAttribute SupplierSearchQuery query,
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return queryHandler
                .searchSuppliers(query.getName(), pageable)
                .map(SupplierPersistenceMapper::toResponse);
    }
}