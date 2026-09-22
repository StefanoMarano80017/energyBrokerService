package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

@WebMvcTest(AgreementPageController.class)
class AgreementPageControllerTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    private final CpoId cpoId = CpoId.newId();
    private final EmspId emspId = EmspId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgreementQueryHandler agreementQueryHandler;

    @MockitoBean
    private AgreementLifecycleCommandHandler lifecycleHandler;

    @MockitoBean
    private CpoQueryHandler cpoQueryHandler;

    @MockitoBean
    private EmspQueryHandler emspQueryHandler;

    @BeforeEach
    void setUpActors() {
        given(cpoQueryHandler.findAll()).willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));
    }

    @Test
    void theAgreementListResolvesBothActorsWithTheirExternalId() throws Exception {
        given(agreementQueryHandler.search(isNull(), isNull(), isNull()))
                .willReturn(List.of(proposedAgreement()));

        mockMvc.perform(get("/ui/agreements"))
                .andExpect(status().isOk())
                .andExpect(view().name("agreements"))
                .andExpect(content().string(containsString("Charge Spa")))
                .andExpect(content().string(containsString("ext: CPO-001")))
                .andExpect(content().string(containsString("Mobility Spa")))
                .andExpect(content().string(containsString("ext: EMSP-001")))
                .andExpect(content().string(containsString(cpoId.value().toString())))
                .andExpect(content().string(containsString(emspId.value().toString())));
    }

    @Test
    void thePendingListResolvesBothActorsWithTheirExternalId() throws Exception {
        given(agreementQueryHandler.search(isNull(), isNull(), eq(AgreementStatus.PROPOSED)))
                .willReturn(List.of(proposedAgreement()));

        mockMvc.perform(get("/ui/agreements/pending"))
                .andExpect(status().isOk())
                .andExpect(view().name("agreements-pending"))
                .andExpect(content().string(containsString("ext: CPO-001")))
                .andExpect(content().string(containsString("ext: EMSP-001")));
    }

    @Test
    void fallsBackToTheRawUuidWhenTheActorCannotBeResolved() throws Exception {
        given(cpoQueryHandler.findAll()).willReturn(List.of());
        given(agreementQueryHandler.search(isNull(), isNull(), isNull()))
                .willReturn(List.of(proposedAgreement()));

        mockMvc.perform(get("/ui/agreements"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(cpoId.value().toString())));
    }

    private Agreement proposedAgreement() {
        return new Agreement(
                AgreementId.newId(),
                cpoId,
                emspId,
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))),
                AgreementStatus.PROPOSED,
                Instant.parse("2026-01-01T10:00:00Z"),
                null
        );
    }
}
