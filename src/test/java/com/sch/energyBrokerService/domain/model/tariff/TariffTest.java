package com.sch.energyBrokerService.domain.model.tariff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TariffTest {

    @Test
    void createsATariffWithTheProvidedValues() {
        Tariff tariff = new Tariff(
                TariffId.newId(),
                TariffType.FLAT,
                new PricePerKwh(new BigDecimal("0.42"))
        );

        assertThat(tariff.id()).isNotNull();
        assertThat(tariff.type()).isEqualTo(TariffType.FLAT);
        assertThat(tariff.pricePerKwh().amount()).isEqualByComparingTo(new BigDecimal("0.4200"));
    }

    @Test
    void rejectsNullMandatoryValues() {
        PricePerKwh positivePrice = new PricePerKwh(new BigDecimal("0.42"));

        assertThatThrownBy(() -> new Tariff(null, TariffType.FLAT, positivePrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TariffId");

        assertThatThrownBy(() -> new Tariff(TariffId.newId(), null, positivePrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TariffType");

        assertThatThrownBy(() -> new Tariff(TariffId.newId(), TariffType.FLAT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PricePerKwh");
    }
}
