package com.sch.energyBrokerService.domain.model.tariff;

import java.util.UUID;

public record TariffId(UUID value) {
    public TariffId {
        if (value == null) {
            throw new IllegalArgumentException("TariffId cannot be null");
        }
    }

    public static TariffId newId() {
        return new TariffId(UUID.randomUUID());
    }
}
