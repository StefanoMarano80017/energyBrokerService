package com.sch.energyBrokerService.domain.model.emsp;

import java.util.UUID;

public record EmspId(UUID value) {

    public EmspId {
        if (value == null) {
            throw new IllegalArgumentException("EmspId cannot be null");
        }
    }

    public static EmspId newId() {
        return new EmspId(UUID.randomUUID());
    }
}
