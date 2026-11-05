package com.sch.energyBrokerService.domain.model.emsp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EmspTest {

    @Test
    void keepsTheExternalIdWhenProvided() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");

        assertThat(emsp.externalId()).isEqualTo("EMSP-001");
    }

    @Test
    void trimsTheExternalId() {
        Emsp emsp = new Emsp(EmspId.newId(), "  EMSP-001  ", "Mobility Spa");

        assertThat(emsp.externalId()).isEqualTo("EMSP-001");
    }

    @Test
    void allowsAnEmspWithoutExternalId() {
        Emsp emsp = new Emsp(EmspId.newId(), null, "Mobility Spa");

        assertThat(emsp.externalId()).isNull();
        assertThat(emsp.name()).isEqualTo("Mobility Spa");
    }

    @Test
    void normalizesBlankExternalIdToNull() {
        Emsp emsp = new Emsp(EmspId.newId(), "  ", "Mobility Spa");

        assertThat(emsp.externalId()).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void rejectsANullOrBlankName(String name) {
        assertThatThrownBy(() -> new Emsp(EmspId.newId(), "EMSP-001", name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }
}
