package com.sch.energyBrokerService.domain.model.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AgreementStatusTest {

    @Test
    void parsesSupportedStatusesCaseInsensitively() {
        assertThat(AgreementStatus.fromString("proposed")).isEqualTo(AgreementStatus.PROPOSED);
        assertThat(AgreementStatus.fromString("ACCEPTED")).isEqualTo(AgreementStatus.ACCEPTED);
        assertThat(AgreementStatus.fromString("rejected")).isEqualTo(AgreementStatus.REJECTED);
        assertThat(AgreementStatus.fromString("cancelled")).isEqualTo(AgreementStatus.CANCELLED);
    }

    @Test
    void rejectsUnsupportedStatusValues() {
        assertThatThrownBy(() -> AgreementStatus.fromString("pending"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid agreement status");
    }
}
