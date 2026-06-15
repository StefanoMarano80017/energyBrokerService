package com.sch.energyBrokerService.domain.model.agreement;

import java.util.UUID;

public record AgreementId(UUID value) {

    public AgreementId {
        if (value == null) {
            throw new IllegalArgumentException("AgreementId cannot be null");
        }
    }

    public static AgreementId newId() {
        return new AgreementId(UUID.randomUUID());
    }
}
