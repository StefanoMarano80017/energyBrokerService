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
import org.mockito.ArgumentCaptor;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.TariffPayload;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

class PublishAgreementCommandHandlerTest {

    private final AgreementRepository agreementRepository = mock(AgreementRepository.class);
    private final CpoRepository cpoRepository = mock(CpoRepository.class);
    private final EmspRepository emspRepository = mock(EmspRepository.class);
    private final PublishAgreementCommandHandler handler =
            new PublishAgreementCommandHandler(agreementRepository, cpoRepository, emspRepository);

    @Test
    void publishesAProposedAgreementForExistingParties() {
        UUID cpoValue = UUID.randomUUID();
        UUID emspValue = UUID.randomUUID();
        CpoId cpoId = new CpoId(cpoValue);
        EmspId emspId = new EmspId(emspValue);
        when(cpoRepository.findById(cpoId)).thenReturn(Optional.of(
                new Cpo(cpoId, null, "Charge Spa", new Coordinate(45, 9))));
        when(emspRepository.findById(emspId)).thenReturn(Optional.of(new Emsp(emspId, null, "Mobility Spa")));
        when(agreementRepository.save(any(Agreement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateAgreementRequest request = new CreateAgreementRequest(
                cpoValue,
                emspValue,
                new TariffPayload(TariffType.TIME_BASED, new BigDecimal("0.35")));

        Agreement result = handler.handle(request);

        assertThat(result.status()).isEqualTo(AgreementStatus.PROPOSED);
        assertThat(result.cpoId()).isEqualTo(cpoId);
        assertThat(result.emspId()).isEqualTo(emspId);
        assertThat(result.tariff().type()).isEqualTo(TariffType.TIME_BASED);
        assertThat(result.tariff().pricePerKwh().amount()).isEqualByComparingTo("0.3500");
        verify(agreementRepository).save(any(Agreement.class));
    }

    @Test
    void doesNotSaveWhenTheCpoDoesNotExist() {
        UUID cpoValue = UUID.randomUUID();
        UUID emspValue = UUID.randomUUID();
        when(cpoRepository.findById(new CpoId(cpoValue))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new CreateAgreementRequest(
                cpoValue,
                emspValue,
                new TariffPayload(TariffType.FLAT, new BigDecimal("0.25")))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cpo not found");

        verify(emspRepository, never()).findById(any());
        verify(agreementRepository, never()).save(any());
    }

    @Test
    void doesNotSaveWhenTheEmspDoesNotExist() {
        UUID cpoValue = UUID.randomUUID();
        UUID emspValue = UUID.randomUUID();
        CpoId cpoId = new CpoId(cpoValue);
        when(cpoRepository.findById(cpoId)).thenReturn(Optional.of(
                new Cpo(cpoId, null, "Charge Spa", new Coordinate(45, 9))));
        when(emspRepository.findById(new EmspId(emspValue))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new CreateAgreementRequest(
                cpoValue,
                emspValue,
                new TariffPayload(TariffType.FLAT, new BigDecimal("0.25")))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Emsp not found");

        verify(agreementRepository, never()).save(any());
    }
}
