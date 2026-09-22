package com.sch.energyBrokerService.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

@WebMvcTest(EmspController.class)
class EmspControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmspCommandHandler commandHandler;

    @MockitoBean
    private EmspQueryHandler queryHandler;

    @Test
    void createsAnEmspWithAnExternalId() throws Exception {
        EmspId id = EmspId.newId();
        given(commandHandler.create(any())).willReturn(new Emsp(id, "EMSP-001", "Mobility Spa"));

        mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mobility Spa","externalId":"EMSP-001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.value().toString()))
                .andExpect(jsonPath("$.externalId").value("EMSP-001"));

        ArgumentCaptor<CreateEmspRequest> captor = ArgumentCaptor.forClass(CreateEmspRequest.class);
        org.mockito.Mockito.verify(commandHandler).create(captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("EMSP-001");
    }

    @Test
    void createsAnEmspWithoutAnExternalId() throws Exception {
        given(commandHandler.create(any()))
                .willReturn(new Emsp(EmspId.newId(), null, "Mobility Spa"));

        mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mobility Spa"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mobility Spa"))
                .andExpect(jsonPath("$.externalId").doesNotExist());
    }

    @Test
    void rejectsADuplicatedExternalIdWithBadRequest() throws Exception {
        willThrow(new IllegalArgumentException("Emsp externalId already in use: EMSP-001"))
                .given(commandHandler).create(any());

        mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mobility Spa","externalId":"EMSP-001"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Emsp externalId already in use: EMSP-001"));
    }

    @Test
    void filtersByExternalId() throws Exception {
        EmspId id = EmspId.newId();
        given(queryHandler.search(null, "EMSP-001"))
                .willReturn(List.of(new Emsp(id, "EMSP-001", "Mobility Spa")));

        mockMvc.perform(get("/emsps").param("externalId", "EMSP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.value().toString()))
                .andExpect(jsonPath("$[0].externalId").value("EMSP-001"));
    }

    @Test
    void keepsTheExistingNameFilter() throws Exception {
        given(queryHandler.search("Mobility", null))
                .willReturn(List.of(new Emsp(EmspId.newId(), null, "Mobility Spa")));

        mockMvc.perform(get("/emsps").param("name", "Mobility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
