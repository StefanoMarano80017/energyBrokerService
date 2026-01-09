package com.sch.energyBrokerService.domain.model.supplier;

import java.util.UUID;

public record SupplierId(UUID value) {
    
    public SupplierId {
        if (value == null) {
            throw new IllegalArgumentException("SupplierId cannot be null");
        }
    }

    public static SupplierId newId() {
        return new SupplierId(UUID.randomUUID());
    }
}
