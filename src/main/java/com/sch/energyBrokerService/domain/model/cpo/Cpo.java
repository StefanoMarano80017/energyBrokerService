package com.sch.energyBrokerService.domain.model.cpo;

import com.sch.energyBrokerService.domain.model.geo.Coordinate;

/**
 * Charge Point Operator: l'operatore che pubblica una tariffa verso un eMSP.
 *
 * <p>La posizione è una singola {@link Coordinate} (riuso del value object esistente),
 * NON una copertura geografica.
 */
public class Cpo {

    private final CpoId id;
    private final String name;
    private final Coordinate location;

    public Cpo(CpoId id, String name, Coordinate location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cpo name is required");
        }
        if (location == null) {
            throw new IllegalArgumentException("Cpo location is required");
        }
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public CpoId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Coordinate location() {
        return location;
    }
}
