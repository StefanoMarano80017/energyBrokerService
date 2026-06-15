package com.sch.energyBrokerService.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.CpoResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.cpo.command.CpoCommandHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cpos")
@RequiredArgsConstructor
@Tag(name = "CPO", description = "Gestione dei Charge Point Operator")
public class CpoController {

    private final CpoCommandHandler commandHandler;
    private final CpoQueryHandler queryHandler;

    @Operation(summary = "Crea un nuovo CPO")
    @PostMapping
    public CpoResponse create(@Valid @RequestBody CreateCpoRequest request) {
        return CpoPersistenceMapper.toResponse(commandHandler.create(request));
    }

    @Operation(summary = "Elenca i CPO")
    @GetMapping
    public List<CpoResponse> findAll() {
        return queryHandler.findAll().stream()
                .map(CpoPersistenceMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Recupera un CPO per id")
    @GetMapping("/{id}")
    public CpoResponse getById(@PathVariable UUID id) {
        return queryHandler.findById(id)
                .map(CpoPersistenceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cpo not found: " + id));
    }
}
