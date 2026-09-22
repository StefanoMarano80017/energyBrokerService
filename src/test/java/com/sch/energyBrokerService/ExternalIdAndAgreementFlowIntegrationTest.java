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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica end-to-end su database reale (H2) del flusso completo:
 * creazione con external id -> lookup tramite external id -> proposta del CPO -> accettazione dell'eMSP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExternalIdAndAgreementFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void anExternalSystemCanCreateAndThenResolveItsOwnEntities() throws Exception {
        String cpoJson = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383,
                                 "externalId":"EXT-CPO-42"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("EXT-CPO-42"))
                .andReturn().getResponse().getContentAsString();

        String internalId = objectMapper.readTree(cpoJson).get("id").asText();

        // Il sistema esterno ritrova l'entità partendo dal proprio identificativo.
        mockMvc.perform(get("/cpos").param("externalId", "EXT-CPO-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(internalId));

        mockMvc.perform(get("/cpos/{id}", internalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("EXT-CPO-42"));
    }

    @Test
    void theExternalIdIsUniqueAcrossCpos() throws Exception {
        createCpo("EXT-CPO-DUP");

        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Power Srl","latitude":45.0,"longitude":9.0,
                                 "externalId":"EXT-CPO-DUP"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("already in use")));
    }

    @Test
    void severalEntitiesMayHaveNoExternalId() throws Exception {
        createCpo(null);
        createCpo(null);

        mockMvc.perform(get("/cpos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void theCpoProposesAnAgreementAndTheEmspAcceptsItFromTheirOwnPages() throws Exception {
        String cpoId = createCpo("EXT-CPO-1");
        String emspId = createEmsp("EXT-EMSP-1");

        // Pagina CPO: l'attore è dichiarato nell'HTML e il form propone l'accordo.
        mockMvc.perform(get("/ui/cpo/agreements"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CPO — Proposta accordo")));

        mockMvc.perform(post("/ui/cpo/agreements")
                        .param("cpoId", cpoId)
                        .param("emspId", emspId)
                        .param("tariff.type", "FLAT")
                        .param("tariff.pricePerKwh", "0.42"))
                .andExpect(status().is3xxRedirection());

        // L'accordo esiste in stato PROPOSED ed è visibile nella inbox dell'eMSP.
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

        // Pagina eMSP: accettazione.
        mockMvc.perform(post("/ui/emsp/agreements/{id}/accept", agreementId)
                        .param("emspId", emspId))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/agreements/{id}", agreementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.acceptedAt").isNotEmpty());

        // Accettato: non compare più fra le proposte in attesa dell'eMSP.
        mockMvc.perform(get("/ui/emsp/agreements").param("emspId", emspId))
                .andExpect(content().string(containsString("No proposed agreement")));
    }

    private String createCpo(String externalId) throws Exception {
        String body = externalId == null
                ? """
                  {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383}
                  """
                : """
                  {"name":"Charge Spa","latitude":45.464211,"longitude":9.191383,
                   "externalId":"%s"}
                  """.formatted(externalId);

        String json = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
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
