package com.sch.energyBrokerService.dev;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.TariffPayload;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.command.PublishAgreementCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Genera accordi sintetici per la demo. Solo profilo dev. Idempotente.
 *
 * <p>Per ogni CPO crea 1..5 accordi verso eMSP casuali, prezzo casuale e distribuzione
 * stato ~70% ACCEPTED / 30% PROPOSED (riusando i casi d'uso publish + accept).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class AgreementGenerator {

    private static final BigDecimal[] PRICES = {
            new BigDecimal("0.35"),
            new BigDecimal("0.40"),
            new BigDecimal("0.45"),
            new BigDecimal("0.50"),
            new BigDecimal("0.55")
    };
    private static final double ACCEPTED_RATIO = 0.70;

    private final PublishAgreementCommandHandler publishHandler;
    private final AgreementLifecycleCommandHandler lifecycleHandler;
    private final AgreementQueryHandler agreementQueryHandler;
    private final CpoQueryHandler cpoQueryHandler;
    private final EmspQueryHandler emspQueryHandler;

    public int generate() {
        if (!agreementQueryHandler.search(null, null, null).isEmpty()) {
            log.info("Accordi già presenti, skip generazione");
            return 0;
        }

        List<Cpo> cpos = cpoQueryHandler.findAll();
        List<Emsp> emsps = emspQueryHandler.search(null);
        if (cpos.isEmpty() || emsps.isEmpty()) {
            log.warn("Impossibile generare accordi: CPO o eMSP mancanti");
            return 0;
        }

        Random random = new Random();
        TariffType[] types = TariffType.values();
        int created = 0;

        for (Cpo cpo : cpos) {
            int howMany = 1 + random.nextInt(5); // 1..5
            for (int i = 0; i < howMany; i++) {
                Emsp emsp = emsps.get(random.nextInt(emsps.size()));
                TariffType type = types[random.nextInt(types.length)];
                BigDecimal price = PRICES[random.nextInt(PRICES.length)];

                Agreement agreement = publishHandler.handle(new CreateAgreementRequest(
                        cpo.id().value(),
                        emsp.id().value(),
                        new TariffPayload(type, price)
                ));
                created++;

                if (random.nextDouble() < ACCEPTED_RATIO) {
                    lifecycleHandler.accept(agreement.id().value());
                }
            }
        }

        log.info("Generati {} accordi", created);
        return created;
    }
}
