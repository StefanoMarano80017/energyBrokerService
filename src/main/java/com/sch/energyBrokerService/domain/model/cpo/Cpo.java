package com.sch.energyBrokerService.domain.model.cpo;

import com.sch.energyBrokerService.domain.model.geo.Coordinate;

/**
 * Charge Point Operator: l'operatore che pubblica una tariffa verso un eMSP.
 *
 * <p>La posizione è una singola {@link Coordinate} (riuso del value object esistente),
 * NON una copertura geografica.
 *
 * <p>{@code externalId} è l'identificativo assegnato dal sistema esterno che gestisce
 * il CPO: è opzionale (può essere {@code null}) e, quando valorizzato, deve essere
 * univoco fra i CPO. Serve ai servizi esterni per mappare le proprie entità su quelle
 * dell'Energy Broker senza conoscerne l'{@link CpoId} interno.
 */
public class Cpo {

    private final CpoId id;
    private final String externalId;
    private final String name;
    private final Coordinate location;

    public Cpo(CpoId id, String externalId, String name, Coordinate location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cpo name is required");
        }
        if (location == null) {
            throw new IllegalArgumentException("Cpo location is required");
        }
        this.id = id;
        this.externalId = normalizeExternalId(externalId);
        this.name = name;
        this.location = location;
    }

    /** Valore assente e valore composto di soli spazi sono equivalenti: entrambi {@code null}. */
    private static String normalizeExternalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public CpoId id() {
        return id;
    }

    public String externalId() {
        return externalId;
    }

    public String name() {
        return name;
    }

    public Coordinate location() {
        return location;
    }
}
