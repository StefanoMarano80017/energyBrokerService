package com.sch.energyBrokerService.dev;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inizializza un set realistico di eMSP italiani. Solo profilo dev. Idempotente.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class EmspLoader {

    /** Elenco facilmente manutenibile di eMSP demo. */
    private static final List<String> EMSP_NAMES = List.of(
            "Enel X Way",
            "A2A E-Mobility",
            "Be Charge",
            "Plenitude On The Road",
            "Duferco Energia",
            "Electrip",
            "Evway",
            "Route220",
            "Nextcharge"
    );

    private final EmspCommandHandler emspCommandHandler;
    private final EmspQueryHandler emspQueryHandler;

    public int load() {
        if (!emspQueryHandler.search(null).isEmpty()) {
            log.info("eMSP già presenti, skip inizializzazione");
            return 0;
        }

        EMSP_NAMES.forEach(name -> emspCommandHandler.create(new CreateEmspRequest(name)));
        log.info("Creati {} eMSP", EMSP_NAMES.size());
        return EMSP_NAMES.size();
    }
}
