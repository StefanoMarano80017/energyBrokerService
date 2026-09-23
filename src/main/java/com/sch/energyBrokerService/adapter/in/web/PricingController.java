package com.sch.energyBrokerService.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.PriceQuoteRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffQuoteResponse;
import com.sch.energyBrokerService.application.service.pricing.query.PriceQuoteQueryHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Calcolo del costo di una ricarica sulle tariffe dei CPO")
public class PricingController {

    private final PriceQuoteQueryHandler quoteHandler;

    @Operation(summary = "Restituisce il costo dei kWh richiesti per ogni tariffa degli accordi ACCEPTED del CPO")
    @PostMapping("/quote")
    public List<TariffQuoteResponse> quote(@Valid @RequestBody PriceQuoteRequest request) {
        return quoteHandler.quote(request.cpoName(), request.kwh());
    }
}
