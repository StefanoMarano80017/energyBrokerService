package com.sch.energyBrokerService.domain.model.tariff;

public enum TariffType {
    FLAT,
    TIME_BASED,
    DYNAMIC;

    public static TariffType fromString(String value) {
        try {
            return TariffType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid tariff type: " + value +
                ". Allowed values: FLAT, TIME_BASED, DYNAMIC"
            );
        }
    }
}
