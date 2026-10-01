package com.sch.energyBrokerService.dev;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Endpoint riservati allo sviluppo locale (solo profilo dev).
 */
@RestController
@RequestMapping("/dev")
@Profile("dev")
@RequiredArgsConstructor
@Tag(name = "Dev", description = "Strumenti di inizializzazione dati (solo profilo dev)")
public class DevController {

    private final DataInitializer dataInitializer;

    @Operation(summary = "Carica CPO ed eMSP dai CSV e genera accordi (idempotente)")
    @PostMapping("/init")
    public Map<String, Integer> init() {
        return dataInitializer.init();
    }

    @Operation(summary = "Svuota tutti i dati di sviluppo")
    @PostMapping("/reset")
    public Map<String, Integer> reset() {
        return dataInitializer.reset();
    }
}
