package com.sch.energyBrokerService.domain.model.tariff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PricePerKwhTest {

    @Test
    void acceptsPositivePricesAndScalesThemToFourDecimalPlaces() {
        PricePerKwh price = new PricePerKwh(new BigDecimal("0.123456"));

        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("0.1235"));
    }

    @Test
    void rejectsNullOrNonPositiveAmounts() {
        assertThatThrownBy(() -> new PricePerKwh(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");

        assertThatThrownBy(() -> new PricePerKwh(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");

        assertThatThrownBy(() -> new PricePerKwh(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");
    }

    @Test
    void multipliesThePriceByTheConsumedKwhAndRoundsToTwoDecimals() {
        PricePerKwh price = new PricePerKwh(new BigDecimal("0.3333"));

        assertThat(price.multiply(new BigDecimal("2.5")))
                .isEqualByComparingTo(new BigDecimal("0.83"));
    }
}
