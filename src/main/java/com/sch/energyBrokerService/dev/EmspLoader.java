package com.sch.energyBrokerService.dev;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carica gli eMSP dal CSV nelle resources. Solo profilo dev. Idempotente.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class EmspLoader {

    private static final String CSV_LOCATION = "emsp.csv";

    private final EmspCommandHandler emspCommandHandler;
    private final EmspQueryHandler emspQueryHandler;

    public int load() {
        if (!emspQueryHandler.search(null, null).isEmpty()) {
            log.info("eMSP già presenti, skip inizializzazione");
            return 0;
        }

        int created = 0;
        ClassPathResource resource = new ClassPathResource(CSV_LOCATION);

        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                emspCommandHandler.create(new CreateEmspRequest(line.trim(), null));
                created++;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Errore nel caricamento di " + CSV_LOCATION, e);
        }

        log.info("Caricati {} eMSP dal CSV", created);
        return created;
    }
}
