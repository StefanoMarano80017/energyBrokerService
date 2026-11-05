package com.sch.energyBrokerService.domain.model.emsp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmspIdTest {

    @Test
    void createsANonNullEmspId() {
        assertThat(EmspId.newId().value()).isNotNull();
    }

    @Test
    void rejectsANullValue() {
        assertThatThrownBy(() -> new EmspId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EmspId");
    }
}
