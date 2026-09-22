package com.sch.energyBrokerService.domain.model.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/**
 * Copre il workflow CPO -> propone -> eMSP -> accetta a livello di aggregato,
 * che le due pagine UI degli attori si limitano a innescare.
 */
class AgreementTest {

    private static Agreement proposed() {
        return Agreement.propose(
                CpoId.newId(),
                EmspId.newId(),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42")))
        );
    }

    @Test
    void theCpoProposalCreatesAProposedAgreement() {
        Agreement agreement = proposed();

        assertThat(agreement.status()).isEqualTo(AgreementStatus.PROPOSED);
        assertThat(agreement.createdAt()).isNotNull();
        assertThat(agreement.acceptedAt()).isNull();
    }

    @Test
    void theEmspAcceptanceMovesTheAgreementToAccepted() {
        Agreement agreement = proposed();

        agreement.accept();

        assertThat(agreement.status()).isEqualTo(AgreementStatus.ACCEPTED);
        assertThat(agreement.acceptedAt()).isNotNull();
    }

    @Test
    void anAlreadyAcceptedAgreementCannotBeAcceptedTwice() {
        Agreement agreement = proposed();
        agreement.accept();

        assertThatThrownBy(agreement::accept)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACCEPTED");
    }

    @Test
    void theEmspRejectionMovesTheAgreementToRejected() {
        Agreement agreement = proposed();

        agreement.reject();

        assertThat(agreement.status()).isEqualTo(AgreementStatus.REJECTED);
        assertThat(agreement.acceptedAt()).isNull();
    }

    @Test
    void aRejectedAgreementCannotBeAccepted() {
        Agreement agreement = proposed();
        agreement.reject();

        assertThatThrownBy(agreement::accept).isInstanceOf(IllegalStateException.class);
    }
}
