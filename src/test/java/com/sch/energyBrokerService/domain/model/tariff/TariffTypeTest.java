package com.sch.energyBrokerService.domain.model.tariff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TariffTypeTest {

    @Test
    void parsesKnownTariffTypesCaseInsensitively() {
        assertThat(TariffType.fromString("flat")).isEqualTo(TariffType.FLAT);
        assertThat(TariffType.fromString("TIME_BASED")).isEqualTo(TariffType.TIME_BASED);
        assertThat(TariffType.fromString("Dynamic")).isEqualTo(TariffType.DYNAMIC);
    }

    @Test
    void rejectsUnsupportedValues() {
        assertThatThrownBy(() -> TariffType.fromString("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid tariff type");
    }

    @Test
    void tariffIdRequiresAValue() {
        assertThatThrownBy(() -> new TariffId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TariffId");
    }
}
