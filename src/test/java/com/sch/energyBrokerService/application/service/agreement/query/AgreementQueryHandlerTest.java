package com.sch.energyBrokerService.application.service.agreement.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

class AgreementQueryHandlerTest {

    private final AgreementRepository repository = mock(AgreementRepository.class);
    private final AgreementQueryHandler handler = new AgreementQueryHandler(repository);

    @Test
    void convertsProvidedFilterIdsAndPassesThroughTheSearchResults() {
        UUID cpoValue = UUID.randomUUID();
        UUID emspValue = UUID.randomUUID();
        List<Agreement> agreements = List.of();
        when(repository.search(new CpoId(cpoValue), new EmspId(emspValue), AgreementStatus.ACCEPTED))
                .thenReturn(agreements);

        assertThat(handler.search(cpoValue, emspValue, AgreementStatus.ACCEPTED)).isSameAs(agreements);

        verify(repository).search(new CpoId(cpoValue), new EmspId(emspValue), AgreementStatus.ACCEPTED);
    }

    @Test
    void wrapsTheRequestedIdWhenLoadingAnAgreement() {
        UUID value = UUID.randomUUID();
        Agreement agreement = mock(Agreement.class);
        when(repository.findById(new AgreementId(value))).thenReturn(Optional.of(agreement));

        assertThat(handler.findById(value)).containsSame(agreement);
    }
}
