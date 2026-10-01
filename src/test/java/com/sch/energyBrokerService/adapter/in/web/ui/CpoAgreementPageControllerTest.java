package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.application.service.agreement.command.PublishAgreementCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

@WebMvcTest(CpoAgreementPageController.class)
@ActiveProfiles("dev")
class CpoAgreementPageControllerTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    private final CpoId cpoId = CpoId.newId();
    private final EmspId emspId = EmspId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CpoQueryHandler cpoQueryHandler;

    @MockitoBean
    private EmspQueryHandler emspQueryHandler;

    @MockitoBean
    private AgreementQueryHandler agreementQueryHandler;

    @MockitoBean
    private PublishAgreementCommandHandler publishHandler;

    @Test
    void rendersThePageStatingThatTheCurrentActorIsTheCpo() throws Exception {
        given(cpoQueryHandler.findAll()).willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));
        given(agreementQueryHandler.search(isNull(), isNull(), isNull())).willReturn(List.of());

        mockMvc.perform(get("/ui/cpo/agreements"))
                .andExpect(status().isOk())
                .andExpect(view().name("agreement-propose-cpo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CPO — Proposta accordo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attore corrente")))
                .andExpect(model().attributeExists("cpos", "emsps", "tariffTypes", "agreements"));
    }

    @Test
    void offersEveryTariffTypeAndBothActorsInTheForm() throws Exception {
        given(cpoQueryHandler.findAll()).willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));
        given(agreementQueryHandler.search(isNull(), isNull(), isNull())).willReturn(List.of());

        mockMvc.perform(get("/ui/cpo/agreements"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Charge Spa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mobility Spa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FLAT")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"tariff.type\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"tariff.pricePerKwh\"")));
    }

    @Test
    void listsTheAgreementsOfTheSelectedCpo() throws Exception {
        given(cpoQueryHandler.findAll()).willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));
        given(agreementQueryHandler.search(eq(cpoId.value()), isNull(), isNull()))
                .willReturn(List.of(proposedAgreement()));

        mockMvc.perform(get("/ui/cpo/agreements").param("cpoId", cpoId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedCpoId", cpoId.value()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PROPOSED")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ext: EMSP-001")));
    }

    @Test
    void proposesAnAgreementReusingThePublishUseCase() throws Exception {
        given(publishHandler.handle(any())).willReturn(proposedAgreement());

        mockMvc.perform(post("/ui/cpo/agreements")
                        .param("cpoId", cpoId.value().toString())
                        .param("emspId", emspId.value().toString())
                        .param("tariff.type", "FLAT")
                        .param("tariff.pricePerKwh", "0.42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/cpo/agreements?cpoId=" + cpoId.value()));

        ArgumentCaptor<CreateAgreementRequest> captor =
                ArgumentCaptor.forClass(CreateAgreementRequest.class);
        verify(publishHandler).handle(captor.capture());
        CreateAgreementRequest sent = captor.getValue();
        assertThat(sent.cpoId()).isEqualTo(cpoId.value());
        assertThat(sent.emspId()).isEqualTo(emspId.value());
        assertThat(sent.tariff().type()).isEqualTo(TariffType.FLAT);
        assertThat(sent.tariff().pricePerKwh()).isEqualByComparingTo("0.42");
    }

    private Agreement proposedAgreement() {
        return new Agreement(
                com.sch.energyBrokerService.domain.model.agreement.AgreementId.newId(),
                cpoId,
                emspId,
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))),
                com.sch.energyBrokerService.domain.model.agreement.AgreementStatus.PROPOSED,
                Instant.parse("2026-01-01T10:00:00Z"),
                null,
                Instant.parse("2026-01-01T10:00:00Z")
        );
    }
}
