package com.sch.energyBrokerService.domain.model.cpo;

import java.util.UUID;

public record CpoId(UUID value) {
    public CpoId {
        if (value == null) {
            throw new IllegalArgumentException("CpoId cannot be null");
        }
    }

    public static CpoId newId() {
        return new CpoId(UUID.randomUUID());
    }
}
