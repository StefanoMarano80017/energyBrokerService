package com.sch.energyBrokerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica il flusso integrato di proposta CPO e accettazione eMSP, incluse le pagine
 * disponibili esclusivamente nel profilo dev.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class CpoAgreementFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void theCpoProposesAnAgreementAndTheEmspAcceptsItFromTheirOwnPages() throws Exception {
        String cpoId = createCpo("EXT-CPO-1");
        String emspId = createEmsp("EXT-EMSP-1");

        mockMvc.perform(get("/ui/cpo/agreements"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CPO — Proposta accordo")));

        mockMvc.perform(post("/ui/cpo/agreements")
                        .param("cpoId", cpoId)
                        .param("emspId", emspId)
                        .param("tariff.type", "FLAT")
                        .param("tariff.pricePerKwh", "0.42"))
                .andExpect(status().is3xxRedirection());

        String agreementsJson = mockMvc.perform(get("/agreements").param("status", "PROPOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();

        JsonNode agreement = objectMapper.readTree(agreementsJson).get(0);
        String agreementId = agreement.get("id").asText();
        assertThat(agreement.get("cpoId").asText()).isEqualTo(cpoId);
        assertThat(agreement.get("emspId").asText()).isEqualTo(emspId);

        mockMvc.perform(get("/ui/emsp/agreements").param("emspId", emspId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("eMSP — Accettazione accordo")))
                .andExpect(content().string(containsString("ext: EXT-CPO-1")))
                .andExpect(content().string(containsString(agreementId)));

        mockMvc.perform(post("/ui/emsp/agreements/{id}/accept", agreementId)
                        .param("emspId", emspId))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/agreements/{id}", agreementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.acceptedAt").isNotEmpty());

        mockMvc.perform(get("/ui/emsp/agreements").param("emspId", emspId))
                .andExpect(content().string(containsString("No proposed agreement")));
    }

    private String createCpo(String externalId) throws Exception {
        String json = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383,
                                 "externalId":"%s"}
                                """.formatted(externalId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asText();
    }

    private String createEmsp(String externalId) throws Exception {
        String json = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mobility Spa","externalId":"%s"}
                                """.formatted(externalId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asText();
    }
}
