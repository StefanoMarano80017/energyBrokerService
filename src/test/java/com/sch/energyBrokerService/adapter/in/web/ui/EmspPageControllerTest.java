package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

@WebMvcTest(EmspPageController.class)
@ActiveProfiles("dev")
class EmspPageControllerTest {

    private final EmspId emspId = EmspId.newId();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmspQueryHandler emspQueryHandler;

    @MockitoBean
    private EmspCommandHandler emspCommandHandler;

    @Test
    void theListShowsBothTheInternalAndTheExternalId() throws Exception {
        given(emspQueryHandler.search(null, null))
                .willReturn(List.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));

        mockMvc.perform(get("/ui/emsps"))
                .andExpect(status().isOk())
                .andExpect(view().name("emsps"))
                .andExpect(content().string(containsString("External Id")))
                .andExpect(content().string(containsString("EMSP-001")))
                .andExpect(content().string(containsString(emspId.value().toString())));
    }

    @Test
    void theCreationFormExposesAnOptionalExternalIdInput() throws Exception {
        given(emspQueryHandler.search(null, null)).willReturn(List.of());

        mockMvc.perform(get("/ui/emsps"))
                .andExpect(content().string(containsString("name=\"externalId\"")));
    }

    @Test
    void createsAnEmspWithTheExternalIdComingFromTheForm() throws Exception {
        given(emspCommandHandler.create(any())).willReturn(new Emsp(emspId, "EMSP-001", "Mobility Spa"));

        mockMvc.perform(post("/ui/emsps")
                        .param("name", "Mobility Spa")
                        .param("externalId", "EMSP-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/emsps"));

        ArgumentCaptor<CreateEmspRequest> captor = ArgumentCaptor.forClass(CreateEmspRequest.class);
        verify(emspCommandHandler).create(captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("EMSP-001");
    }

    @Test
    void theEditFormIsPrefilledWithTheExternalId() throws Exception {
        given(emspQueryHandler.findById(emspId.value()))
                .willReturn(Optional.of(new Emsp(emspId, "EMSP-001", "Mobility Spa")));

        mockMvc.perform(get("/ui/emsps/{id}/edit", emspId.value()))
                .andExpect(status().isOk())
                .andExpect(view().name("emsp-edit"))
                .andExpect(content().string(containsString("name=\"externalId\"")))
                .andExpect(content().string(containsString("value=\"EMSP-001\"")));
    }
}
