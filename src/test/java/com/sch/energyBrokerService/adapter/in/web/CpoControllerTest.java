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
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.application.service.cpo.command.CpoCommandHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

@WebMvcTest(CpoController.class)
class CpoControllerTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CpoCommandHandler commandHandler;

    @MockitoBean
    private CpoQueryHandler queryHandler;

    @Test
    void createsACpoWithAnExternalId() throws Exception {
        CpoId id = CpoId.newId();
        given(commandHandler.create(any())).willReturn(new Cpo(id, "CPO-001", "Charge Spa", MILAN));

        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383,
                                 "externalId":"CPO-001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.value().toString()))
                .andExpect(jsonPath("$.externalId").value("CPO-001"));

        ArgumentCaptor<CreateCpoRequest> captor = ArgumentCaptor.forClass(CreateCpoRequest.class);
        org.mockito.Mockito.verify(commandHandler).create(captor.capture());
        assertThat(captor.getValue().externalId()).isEqualTo("CPO-001");
    }

    @Test
    void createsACpoWithoutAnExternalId() throws Exception {
        CpoId id = CpoId.newId();
        given(commandHandler.create(any())).willReturn(new Cpo(id, null, "Charge Spa", MILAN));

        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").doesNotExist());
    }

    @Test
    void rejectsADuplicatedExternalIdWithBadRequest() throws Exception {
        willThrow(new IllegalArgumentException("Cpo externalId already in use: CPO-001"))
                .given(commandHandler).create(any());

        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383,
                                 "externalId":"CPO-001"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cpo externalId already in use: CPO-001"));
    }

    @Test
    void exposesTheExternalIdWhenListing() throws Exception {
        given(queryHandler.search(null))
                .willReturn(List.of(new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN)));

        mockMvc.perform(get("/cpos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("CPO-001"));
    }

    @Test
    void filtersByExternalId() throws Exception {
        CpoId id = CpoId.newId();
        given(queryHandler.search("CPO-001"))
                .willReturn(List.of(new Cpo(id, "CPO-001", "Charge Spa", MILAN)));

        mockMvc.perform(get("/cpos").param("externalId", "CPO-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.value().toString()))
                .andExpect(jsonPath("$[0].externalId").value("CPO-001"));
    }

    @Test
    void returnsAnEmptyListForAnUnknownExternalId() throws Exception {
        given(queryHandler.search("CPO-999")).willReturn(List.of());

        mockMvc.perform(get("/cpos").param("externalId", "CPO-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void exposesTheExternalIdWhenFetchingById() throws Exception {
        UUID id = UUID.randomUUID();
        given(queryHandler.findById(id))
                .willReturn(java.util.Optional.of(new Cpo(new CpoId(id), "CPO-001", "Charge Spa", MILAN)));

        mockMvc.perform(get("/cpos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("CPO-001"));
    }
}
