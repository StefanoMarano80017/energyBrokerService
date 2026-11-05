package com.sch.energyBrokerService.domain.model.cpo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CpoIdTest {

    @Test
    void createsANonNullCpoId() {
        assertThat(CpoId.newId().value()).isNotNull();
    }

    @Test
    void rejectsANullValue() {
        assertThatThrownBy(() -> new CpoId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CpoId");
    }
}
