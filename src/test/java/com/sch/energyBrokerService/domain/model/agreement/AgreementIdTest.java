package com.sch.energyBrokerService.domain.model.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AgreementIdTest {

    @Test
    void createsANonNullAgreementId() {
        AgreementId agreementId = AgreementId.newId();

        assertThat(agreementId).isNotNull();
        assertThat(agreementId.value()).isNotNull();
    }

    @Test
    void rejectsANullAgreementIdValue() {
        assertThatThrownBy(() -> new AgreementId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AgreementId");
    }
}
