package com.sch.energyBrokerService.dev;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaAgreementRepository;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaCpoRepository;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaEmspRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestratore dei dati di sviluppo. Solo profilo dev.
 *
 * <p>init(): carica CPO ed eMSP dai CSV e genera accordi (i loader sono idempotenti).
 * reset(): svuota i repository senza ricaricare dati.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CpoCsvLoader cpoCsvLoader;
    private final EmspLoader emspLoader;
    private final AgreementGenerator agreementGenerator;

    private final JpaAgreementRepository jpaAgreementRepository;
    private final JpaCpoRepository jpaCpoRepository;
    private final JpaEmspRepository jpaEmspRepository;

    @Transactional
    public Map<String, Integer> init() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("cpos", cpoCsvLoader.load());
        summary.put("emsps", emspLoader.load());
        summary.put("agreements", agreementGenerator.generate());
        return summary;
    }

    @Transactional
    public Map<String, Integer> reset() {
        log.info("Reset dati di sviluppo: pulizia repository");
        jpaAgreementRepository.deleteAllInBatch();
        jpaCpoRepository.deleteAllInBatch();
        jpaEmspRepository.deleteAllInBatch();

        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("cpos", 0);
        summary.put("emsps", 0);
        summary.put("agreements", 0);
        return summary;
    }
}
