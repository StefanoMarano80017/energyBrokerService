package com.sch.energyBrokerService.domain.model.tariff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TariffIdTest {

    @Test
    void createsANonNullTariffId() {
        assertThat(TariffId.newId().value()).isNotNull();
    }

    @Test
    void rejectsANullValue() {
        assertThatThrownBy(() -> new TariffId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TariffId");
    }
}
