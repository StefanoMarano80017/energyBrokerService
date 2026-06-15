package com.sch.energyBrokerService.domain.model.agreement;

public enum AgreementStatus {
    PROPOSED,
    ACCEPTED,
    REJECTED,
    CANCELLED;

    public static AgreementStatus fromString(String value) {
        try {
            return AgreementStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid agreement status: " + value +
                ". Allowed values: PROPOSED, ACCEPTED, REJECTED, CANCELLED"
            );
        }
    }
}
