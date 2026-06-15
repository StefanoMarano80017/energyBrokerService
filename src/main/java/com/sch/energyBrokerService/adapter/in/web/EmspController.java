package com.sch.energyBrokerService.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.EmspSearchQuery;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.EmspResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/emsps")
@RequiredArgsConstructor
@Tag(name = "eMSP", description = "Gestione degli e-Mobility Service Provider")
public class EmspController {

    private final EmspCommandHandler commandHandler;
    private final EmspQueryHandler queryHandler;

    @Operation(summary = "Crea un nuovo eMSP")
    @PostMapping
    public EmspResponse create(@Valid @RequestBody CreateEmspRequest request) {
        return EmspPersistenceMapper.toResponse(commandHandler.create(request));
    }

    @Operation(summary = "Elenca gli eMSP, con filtro opzionale sul nome")
    @GetMapping
    public List<EmspResponse> search(@ModelAttribute EmspSearchQuery query) {
        return queryHandler.search(query.getName()).stream()
                .map(EmspPersistenceMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Recupera un eMSP per id")
    @GetMapping("/{id}")
    public EmspResponse getById(@PathVariable UUID id) {
        return queryHandler.findById(id)
                .map(EmspPersistenceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));
    }
}
