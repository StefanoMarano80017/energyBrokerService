package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

@WebMvcTest(EmspAgreementPageController.class)
class EmspAgreementPageControllerTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    private final CpoId cpoId = CpoId.newId();
    private final EmspId emspId = EmspId.newId();
    private final AgreementId agreementId = AgreementId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CpoQueryHandler cpoQueryHandler;

    @MockitoBean
    private EmspQueryHandler emspQueryHandler;

    @MockitoBean
    private AgreementQueryHandler agreementQueryHandler;

    @MockitoBean
    private AgreementLifecycleCommandHandler lifecycleHandler;

    @Test
    void rendersThePageStatingThatTheCurrentActorIsTheEmsp() throws Exception {
        givenOneProposedAgreement(null);

        mockMvc.perform(get("/ui/emsp/agreements"))
                .andExpect(status().isOk())
                .andExpect(view().name("agreement-accept-emsp"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("eMSP — Accettazione accordo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attore corrente")))
                .andExpect(model().attributeExists("emsps", "agreements", "cpoById"));
    }

    @Test
    void listsOnlyTheProposedAgreementsResolvingTheProposingCpo() throws Exception {
        givenOneProposedAgreement(null);

        mockMvc.perform(get("/ui/emsp/agreements"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Charge Spa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ext: CPO-001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Accept")));

        verify(agreementQueryHandler).search(isNull(), isNull(), eq(AgreementStatus.PROPOSED));
    }

    @Test
    void filtersTheInboxByTheSelectedEmsp() throws Exception {
        givenOneProposedAgreement(emspId.value());

        mockMvc.perform(get("/ui/emsp/agreements").param("emspId", emspId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedEmspId", emspId.value()));

        verify(agreementQueryHandler).search(isNull(), eq(emspId.value()), eq(AgreementStatus.PROPOSED));
    }

    @Test
    void acceptsAnAgreementReusingTheLifecycleUseCase() throws Exception {
        given(lifecycleHandler.accept(agreementId.value())).willReturn(acceptedAgreement());

        mockMvc.perform(post("/ui/emsp/agreements/{id}/accept", agreementId.value())
                        .param("emspId", emspId.value().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/emsp/agreements?emspId=" + emspId.value()));

        verify(lifecycleHandler).accept(agreementId.value());
    }

    @Test
    void acceptsAnAgreementWithoutASelectedEmsp() throws Exception {
        given(lifecycleHandler.accept(agreementId.value())).willReturn(acceptedAgreement());

        mockMvc.perform(post("/ui/emsp/agreements/{id}/accept", agreementId.value()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/emsp/agreements"));
    }

    @Test
    void rejectsAnAgreementReusingTheLifecycleUseCase() throws Exception {
        given(lifecycleHandler.reject(agreementId.value())).willReturn(proposedAgreement());

        mockMvc.perform(post("/ui/emsp/agreements/{id}/reject", agreementId.value())
                        .param("emspId", emspId.value().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/emsp/agreements?emspId=" + emspId.value()));

        verify(lifecycleHandler).reject(agreementId.value());
    }

    private void givenOneProposedAgreement(UUID filteredEmspId) {
        given(cpoQueryHandler.findAll()).willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));
        given(agreementQueryHandler.search(null, filteredEmspId, AgreementStatus.PROPOSED))
                .willReturn(List.of(proposedAgreement()));
    }

    private Agreement proposedAgreement() {
        return new Agreement(
                agreementId,
                cpoId,
                emspId,
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))),
                AgreementStatus.PROPOSED,
                Instant.parse("2026-01-01T10:00:00Z"),
                null
        );
    }

    private Agreement acceptedAgreement() {
        Agreement agreement = proposedAgreement();
        agreement.accept();
        return agreement;
    }
}
