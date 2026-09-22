package com.sch.energyBrokerService.dev;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.application.service.cpo.command.CpoCommandHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carica i CPO da un CSV nelle resources (classpath), riusando il caso d'uso di creazione.
 * Solo profilo dev. Idempotente: carica solo se non esistono già CPO.
 *
 * <p>Parsing posizionale (name, latitude, longitude): la prima riga è sempre trattata
 * come header e scartata, indipendentemente dai nomi delle colonne.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class CpoCsvLoader {

    private static final String CSV_LOCATION = "cpos.csv";

    private final CpoCommandHandler cpoCommandHandler;
    private final CpoQueryHandler cpoQueryHandler;

    public int load() {
        if (!cpoQueryHandler.findAll().isEmpty()) {
            log.info("CPO già presenti, skip caricamento CSV");
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
                String[] cols = line.split(",");
                if (cols.length < 3) {
                    log.warn("Riga CSV ignorata (colonne insufficienti): {}", line);
                    continue;
                }
                String name = cols[0].trim();
                double latitude = Double.parseDouble(cols[1].trim());
                double longitude = Double.parseDouble(cols[2].trim());

                cpoCommandHandler.create(new CreateCpoRequest(name, latitude, longitude, null));
                created++;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Errore nel caricamento di " + CSV_LOCATION, e);
        }

        log.info("Caricati {} CPO dal CSV", created);
        return created;
    }
}
