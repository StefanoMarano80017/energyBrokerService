package com.sch.energyBrokerService.application.service.agreement.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

class AgreementLifecycleCommandHandlerTest {

    private final AgreementRepository repository = mock(AgreementRepository.class);
    private final AgreementLifecycleCommandHandler handler = new AgreementLifecycleCommandHandler(repository);

    @Test
    void acceptsAndSavesTheLoadedAgreement() {
        UUID id = UUID.randomUUID();
        Agreement agreement = Agreement.propose(
                new CpoId(UUID.randomUUID()),
                new EmspId(UUID.randomUUID()),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.25"))));
        when(repository.findById(new AgreementId(id))).thenReturn(Optional.of(agreement));
        when(repository.save(agreement)).thenReturn(agreement);

        Agreement result = handler.accept(id);

        assertThat(result.status()).isEqualTo(AgreementStatus.ACCEPTED);
        assertThat(result.acceptedAt()).isNotNull();
        verify(repository).save(agreement);
    }

    @Test
    void reportsWhenTheAgreementDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(new AgreementId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.accept(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsAndSavesTheLoadedAgreement() {
        Agreement agreement = proposedAgreement();
        UUID id = agreement.id().value();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));
        when(repository.save(agreement)).thenReturn(agreement);

        Agreement result = handler.reject(id);

        assertThat(result.status()).isEqualTo(AgreementStatus.REJECTED);
        assertThat(result.acceptedAt()).isNull();
        verify(repository).save(agreement);
    }

    @Test
    void cancelsAProposedAgreement() {
        Agreement agreement = proposedAgreement();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));
        when(repository.save(agreement)).thenReturn(agreement);

        Agreement result = handler.cancel(agreement.id().value());

        assertThat(result.status()).isEqualTo(AgreementStatus.CANCELLED);
        verify(repository).save(agreement);
    }

    @Test
    void updatesTheTariffAndSavesTheLoadedAgreement() {
        Agreement agreement = proposedAgreement();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));
        when(repository.save(agreement)).thenReturn(agreement);

        Agreement result = handler.updateTariff(
                agreement.id().value(), TariffType.DYNAMIC, new BigDecimal("0.37"));

        assertThat(result.tariff().type()).isEqualTo(TariffType.DYNAMIC);
        assertThat(result.tariff().pricePerKwh().amount()).isEqualByComparingTo("0.3700");
        verify(repository).save(agreement);
    }

    @Test
    void doesNotSaveWhenTheDomainRejectsAnInvalidTransition() {
        Agreement agreement = proposedAgreement();
        agreement.accept();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));

        assertThatThrownBy(() -> handler.reject(agreement.id().value()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACCEPTED");

        verify(repository, never()).save(any());
    }

    @Test
    void doesNotSaveWhenCancellationIsNotAllowedByTheDomain() {
        Agreement agreement = proposedAgreement();
        agreement.reject();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));

        assertThatThrownBy(() -> handler.cancel(agreement.id().value()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REJECTED");

        verify(repository, never()).save(any());
    }

    @Test
    void doesNotSaveWhenTheNewPriceIsInvalid() {
        Agreement agreement = proposedAgreement();
        when(repository.findById(agreement.id())).thenReturn(Optional.of(agreement));

        assertThatThrownBy(() -> handler.updateTariff(
                agreement.id().value(), TariffType.FLAT, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");

        assertThat(agreement.tariff().type()).isEqualTo(TariffType.FLAT);
        assertThat(agreement.tariff().pricePerKwh().amount()).isEqualByComparingTo("0.2500");
        verify(repository, never()).save(any());
    }

    private static Agreement proposedAgreement() {
        return Agreement.propose(
                new CpoId(UUID.randomUUID()),
                new EmspId(UUID.randomUUID()),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.25"))));
    }
}
