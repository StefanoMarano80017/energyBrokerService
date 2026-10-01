package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.application.service.cpo.command.CpoCommandHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

@WebMvcTest(CpoPageController.class)
@ActiveProfiles("dev")
class CpoPageControllerTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    private final CpoId cpoId = CpoId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CpoQueryHandler cpoQueryHandler;

    @MockitoBean
    private CpoCommandHandler cpoCommandHandler;

    @Test
    void theListShowsBothTheInternalAndTheExternalId() throws Exception {
        given(cpoQueryHandler.findAll())
                .willReturn(List.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));

        mockMvc.perform(get("/ui/cpos"))
                .andExpect(status().isOk())
                .andExpect(view().name("cpos"))
                .andExpect(content().string(containsString("External Id")))
                .andExpect(content().string(containsString("CPO-001")))
                .andExpect(content().string(containsString(cpoId.value().toString())));
    }

    @Test
    void theCreationFormExposesAnOptionalExternalIdInput() throws Exception {
        given(cpoQueryHandler.findAll()).willReturn(List.of());

        mockMvc.perform(get("/ui/cpos"))
                .andExpect(content().string(containsString("name=\"externalId\"")));
    }

    @Test
    void theListRendersACpoWithoutExternalId() throws Exception {
        given(cpoQueryHandler.findAll())
                .willReturn(List.of(new Cpo(cpoId, null, "Charge Spa", MILAN)));

        mockMvc.perform(get("/ui/cpos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Charge Spa")));
    }

    @Test
    void createsACpoWithTheExternalIdComingFromTheForm() throws Exception {
        given(cpoCommandHandler.create(any())).willReturn(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN));

        mockMvc.perform(post("/ui/cpos")
                        .param("name", "Charge Spa")
                        .param("latitude", "45.464211")
                        .param("longitude", "9.191383")
                        .param("externalId", "CPO-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/cpos"));

        ArgumentCaptor<CreateCpoRequest> captor = ArgumentCaptor.forClass(CreateCpoRequest.class);
        verify(cpoCommandHandler).create(captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("CPO-001");
    }

    @Test
    void theEditFormIsPrefilledWithTheExternalId() throws Exception {
        given(cpoQueryHandler.findById(cpoId.value()))
                .willReturn(Optional.of(new Cpo(cpoId, "CPO-001", "Charge Spa", MILAN)));

        mockMvc.perform(get("/ui/cpos/{id}/edit", cpoId.value()))
                .andExpect(status().isOk())
                .andExpect(view().name("cpo-edit"))
                .andExpect(content().string(containsString("name=\"externalId\"")))
                .andExpect(content().string(containsString("value=\"CPO-001\"")));
    }

    @Test
    void updatesTheExternalIdFromTheEditForm() throws Exception {
        given(cpoCommandHandler.update(eq(cpoId.value()), any()))
                .willReturn(new Cpo(cpoId, "CPO-002", "Charge Spa", MILAN));

        mockMvc.perform(post("/ui/cpos/{id}/update", cpoId.value())
                        .param("name", "Charge Spa")
                        .param("latitude", "45.464211")
                        .param("longitude", "9.191383")
                        .param("externalId", "CPO-002"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/cpos"));

        ArgumentCaptor<CreateCpoRequest> captor = ArgumentCaptor.forClass(CreateCpoRequest.class);
        verify(cpoCommandHandler).update(eq(cpoId.value()), captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("CPO-002");
    }
}
