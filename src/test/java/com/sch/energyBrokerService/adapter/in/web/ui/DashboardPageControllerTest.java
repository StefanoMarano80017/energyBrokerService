package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;

class DashboardPageControllerTest {

    private final CpoQueryHandler cpoQueryHandler = mock(CpoQueryHandler.class);
    private final EmspQueryHandler emspQueryHandler = mock(EmspQueryHandler.class);
    private final AgreementQueryHandler agreementQueryHandler = mock(AgreementQueryHandler.class);
    private final DashboardPageController controller =
            new DashboardPageController(cpoQueryHandler, emspQueryHandler, agreementQueryHandler);

    @Test
    void populatesDashboardCountsFromTheQueryHandlers() {
        when(cpoQueryHandler.findAll()).thenReturn(List.of(mock(Cpo.class)));
        when(emspQueryHandler.search(null, null)).thenReturn(List.of(mock(Emsp.class)));
        when(agreementQueryHandler.search(null, null, null)).thenReturn(List.of(mock(Agreement.class)));
        when(agreementQueryHandler.search(null, null, AgreementStatus.PROPOSED)).thenReturn(List.of());
        when(agreementQueryHandler.search(null, null, AgreementStatus.ACCEPTED))
                .thenReturn(List.of(mock(Agreement.class)));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.dashboard(model);

        assertThat(view).isEqualTo("dashboard");
        assertThat(model).containsEntry("cpoCount", 1)
                .containsEntry("emspCount", 1)
                .containsEntry("agreementCount", 1)
                .containsEntry("proposedCount", 0)
                .containsEntry("acceptedCount", 1);
        verify(agreementQueryHandler).search(null, null, AgreementStatus.ACCEPTED);
    }
}
