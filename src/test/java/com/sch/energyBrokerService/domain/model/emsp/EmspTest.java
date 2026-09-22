package com.sch.energyBrokerService.domain.model.emsp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmspTest {

    @Test
    void keepsTheExternalIdWhenProvided() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");

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

    @Test
    void stillRejectsABlankName() {
        assertThatThrownBy(() -> new Emsp(EmspId.newId(), "EMSP-001", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }
}
