package com.sch.energyBrokerService.domain.model.tariff;

/**
 * Definizione economica di una tariffa.
 *
 * <p>Non è più legata a un fornitore o a una copertura geografica: è un value object
 * indipendente che vive all'interno dell'aggregato {@code Agreement}.
 */
public class Tariff {

    private final TariffId id;
    private final TariffType type;
    private final PricePerKwh pricePerKwh;

    public Tariff(
        TariffId id,
        TariffType type,
        PricePerKwh pricePerKwh
    ) {
        if (id == null) {
            throw new IllegalArgumentException("TariffId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("TariffType is required");
        }
        if (pricePerKwh == null) {
            throw new IllegalArgumentException("PricePerKwh is required");
        }
        this.id = id;
        this.type = type;
        this.pricePerKwh = pricePerKwh;
    }

    public TariffId id() {
        return id;
    }

    public TariffType type() {
        return type;
    }

    public PricePerKwh pricePerKwh() {
        return pricePerKwh;
    }
}
