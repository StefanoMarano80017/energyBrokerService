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

    @Test
    void aProposedAgreementCanBeCancelled() {
        Agreement agreement = proposed();

        agreement.cancel();

        assertThat(agreement.status()).isEqualTo(AgreementStatus.CANCELLED);
        assertThat(agreement.lastModifiedAt()).isNotNull();
    }

    @Test
    void anAcceptedAgreementCanAlsoBeCancelled() {
        Agreement agreement = proposed();
        agreement.accept();

        agreement.cancel();

        assertThat(agreement.status()).isEqualTo(AgreementStatus.CANCELLED);
    }

    @Test
    void aRejectedAgreementCannotBeCancelled() {
        Agreement agreement = proposed();
        agreement.reject();

        assertThatThrownBy(agreement::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    void updateTariffReplacesTheTariffTypeAndPrice() {
        Agreement agreement = proposed();
        Tariff originalTariff = agreement.tariff();

        agreement.updateTariff(TariffType.TIME_BASED, new PricePerKwh(new BigDecimal("0.58")));

        assertThat(agreement.tariff().id()).isEqualTo(originalTariff.id());
        assertThat(agreement.tariff().type()).isEqualTo(TariffType.TIME_BASED);
        assertThat(agreement.tariff().pricePerKwh().amount()).isEqualByComparingTo(new BigDecimal("0.5800"));
        assertThat(agreement.lastModifiedAt()).isNotNull();
    }

    @Test
    void constructorRejectsMissingMandatoryAgreementData() {
        Tariff tariff = new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42")));

        assertThatThrownBy(() -> new Agreement(null, CpoId.newId(), EmspId.newId(), tariff, AgreementStatus.PROPOSED, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AgreementId");

        assertThatThrownBy(() -> new Agreement(AgreementId.newId(), null, EmspId.newId(), tariff, AgreementStatus.PROPOSED, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CpoId");

        assertThatThrownBy(() -> new Agreement(AgreementId.newId(), CpoId.newId(), null, tariff, AgreementStatus.PROPOSED, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EmspId");

        assertThatThrownBy(() -> new Agreement(AgreementId.newId(), CpoId.newId(), EmspId.newId(), null, AgreementStatus.PROPOSED, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tariff");

        assertThatThrownBy(() -> new Agreement(AgreementId.newId(), CpoId.newId(), EmspId.newId(), tariff, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AgreementStatus");

        assertThatThrownBy(() -> new Agreement(AgreementId.newId(), CpoId.newId(), EmspId.newId(), tariff, AgreementStatus.PROPOSED, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt");
    }
}
