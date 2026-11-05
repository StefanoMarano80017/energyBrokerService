package com.sch.energyBrokerService.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.command.PublishAgreementCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

@WebMvcTest(AgreementController.class)
class AgreementControllerTest {

    private static final UUID CPO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID EMSP_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID AGREEMENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublishAgreementCommandHandler publishHandler;

    @MockitoBean
    private AgreementLifecycleCommandHandler lifecycleHandler;

    @MockitoBean
    private AgreementQueryHandler queryHandler;

    @Test
    void publishesAnAgreementFromTheRequest() throws Exception {
        Agreement agreement = agreement(AgreementStatus.PROPOSED);
        given(publishHandler.handle(any(CreateAgreementRequest.class))).willReturn(agreement);

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                 "emspId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                                 "tariff":{"type":"FLAT","pricePerKwh":0.25}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AGREEMENT_ID.toString()))
                .andExpect(jsonPath("$.cpoId").value(CPO_ID.toString()))
                .andExpect(jsonPath("$.emspId").value(EMSP_ID.toString()))
                .andExpect(jsonPath("$.status").value("PROPOSED"))
                .andExpect(jsonPath("$.tariff.type").value("FLAT"))
                .andExpect(jsonPath("$.tariff.pricePerKwh").value(0.25));

        ArgumentCaptor<CreateAgreementRequest> captor = ArgumentCaptor.forClass(CreateAgreementRequest.class);
        verify(publishHandler).handle(captor.capture());
        assertThat(captor.getValue().cpoId()).isEqualTo(CPO_ID);
        assertThat(captor.getValue().emspId()).isEqualTo(EMSP_ID);
        assertThat(captor.getValue().tariff().pricePerKwh()).isEqualByComparingTo("0.25");
    }

    @Test
    void rejectsPublishingAnInvalidRequest() throws Exception {
        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":null,"emspId":null,"tariff":null}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publishHandler);
    }

    @Test
    void acceptsAnAgreement() throws Exception {
        given(lifecycleHandler.accept(AGREEMENT_ID)).willReturn(agreement(AgreementStatus.ACCEPTED));

        mockMvc.perform(post("/agreements/{id}/accept", AGREEMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(lifecycleHandler).accept(AGREEMENT_ID);
    }

    @Test
    void rejectsAnAgreement() throws Exception {
        given(lifecycleHandler.reject(AGREEMENT_ID)).willReturn(agreement(AgreementStatus.REJECTED));

        mockMvc.perform(post("/agreements/{id}/reject", AGREEMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(lifecycleHandler).reject(AGREEMENT_ID);
    }

    @Test
    void cancelsAnAgreement() throws Exception {
        given(lifecycleHandler.cancel(AGREEMENT_ID)).willReturn(agreement(AgreementStatus.CANCELLED));

        mockMvc.perform(post("/agreements/{id}/cancel", AGREEMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(lifecycleHandler).cancel(AGREEMENT_ID);
    }

    @Test
    void updatesAnAgreementsTariff() throws Exception {
        given(lifecycleHandler.updateTariff(AGREEMENT_ID, TariffType.TIME_BASED, new BigDecimal("0.35")))
                .willReturn(agreement(AgreementStatus.PROPOSED, TariffType.TIME_BASED, "0.35"));

        mockMvc.perform(post("/agreements/{id}/update-tariff", AGREEMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tariff":{"type":"TIME_BASED","pricePerKwh":0.35}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tariff.type").value("TIME_BASED"))
                .andExpect(jsonPath("$.tariff.pricePerKwh").value(0.35));

        verify(lifecycleHandler).updateTariff(AGREEMENT_ID, TariffType.TIME_BASED, new BigDecimal("0.35"));
    }

    @Test
    void rejectsATariffUpdateWithoutATariff() throws Exception {
        mockMvc.perform(post("/agreements/{id}/update-tariff", AGREEMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tariff":null}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(lifecycleHandler);
    }

    @Test
    void searchesAgreementsUsingTheProvidedFilters() throws Exception {
        given(queryHandler.search(CPO_ID, EMSP_ID, AgreementStatus.PROPOSED))
                .willReturn(List.of(agreement(AgreementStatus.PROPOSED)));

        mockMvc.perform(get("/agreements")
                        .param("cpoId", CPO_ID.toString())
                        .param("emspId", EMSP_ID.toString())
                        .param("status", "PROPOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PROPOSED"));

        verify(queryHandler).search(CPO_ID, EMSP_ID, AgreementStatus.PROPOSED);
    }

    @Test
    void returnsAnAgreementById() throws Exception {
        given(queryHandler.findById(AGREEMENT_ID))
                .willReturn(Optional.of(agreement(AgreementStatus.PROPOSED)));

        mockMvc.perform(get("/agreements/{id}", AGREEMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AGREEMENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("PROPOSED"));

        verify(queryHandler).findById(AGREEMENT_ID);
    }

    @Test
    void returnsNotFoundForAnUnknownAgreement() throws Exception {
        given(queryHandler.findById(AGREEMENT_ID)).willReturn(Optional.empty());

        mockMvc.perform(get("/agreements/{id}", AGREEMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Agreement not found: " + AGREEMENT_ID));

        verify(queryHandler).findById(AGREEMENT_ID);
    }

    @Test
    void mapsAnInvalidLifecycleTransitionToConflict() throws Exception {
        willThrow(new IllegalStateException("Cannot accept an agreement in status ACCEPTED"))
                .given(lifecycleHandler).accept(AGREEMENT_ID);

        mockMvc.perform(post("/agreements/{id}/accept", AGREEMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot accept an agreement in status ACCEPTED"));
    }

    private static Agreement agreement(AgreementStatus status) {
        return agreement(status, TariffType.FLAT, "0.25");
    }

    private static Agreement agreement(AgreementStatus status, TariffType type, String pricePerKwh) {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        return new Agreement(
                new AgreementId(AGREEMENT_ID),
                new CpoId(CPO_ID),
                new EmspId(EMSP_ID),
                new Tariff(new TariffId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")),
                        type, new PricePerKwh(new BigDecimal(pricePerKwh))),
                status,
                createdAt,
                status == AgreementStatus.ACCEPTED ? createdAt.plusSeconds(60) : null,
                createdAt.plusSeconds(120));
    }
}
