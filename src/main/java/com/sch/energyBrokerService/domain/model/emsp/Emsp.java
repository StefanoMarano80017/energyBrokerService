package com.sch.energyBrokerService.domain.model.emsp;

/**
 * e-Mobility Service Provider: l'operatore che riceve e accetta una tariffa proposta da un CPO.
 */
public class Emsp {

    private final EmspId id;
    private final String name;

    public Emsp(EmspId id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Emsp name is required");
        }
        this.id = id;
        this.name = name;
    }

    public EmspId id() {
        return id;
    }

    public String name() {
        return name;
    }
}
