package com.sch.energyBrokerService.domain.model.emsp;

/**
 * e-Mobility Service Provider: l'operatore che riceve e accetta una tariffa proposta da un CPO.
 *
 * <p>{@code externalId} è l'identificativo assegnato dal sistema esterno che gestisce
 * l'eMSP: è opzionale (può essere {@code null}) e, quando valorizzato, deve essere
 * univoco fra gli eMSP.
 */
public class Emsp {

    private final EmspId id;
    private final String externalId;
    private final String name;

    public Emsp(EmspId id, String externalId, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Emsp name is required");
        }
        this.id = id;
        this.externalId = normalizeExternalId(externalId);
        this.name = name;
    }

    /** Valore assente e valore composto di soli spazi sono equivalenti: entrambi {@code null}. */
    private static String normalizeExternalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public EmspId id() {
        return id;
    }

    public String externalId() {
        return externalId;
    }

    public String name() {
        return name;
    }
}
