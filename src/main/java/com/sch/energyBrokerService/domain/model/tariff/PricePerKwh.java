package com.sch.energyBrokerService.domain.model.tariff;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PricePerKwh(BigDecimal amount) {

    public PricePerKwh {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        amount = amount.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal multiply(BigDecimal kwh) {
        return amount.multiply(kwh).setScale(2, RoundingMode.HALF_UP);
    }
}
