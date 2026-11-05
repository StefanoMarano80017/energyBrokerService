package com.sch.energyBrokerService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CpoEmspResourceFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest(name = "CPO externalId {0} is accepted and normalized")
    @MethodSource("optionalCpoExternalIds")
    void createsCposWithAnAbsentOrBlankExternalId(String description, String externalIdField) throws Exception {
        String name = unique("CPO");
        String body = """
                {"name":"%s","latitude":45.0,"longitude":9.0%s}
                """.formatted(name, externalIdField);

        MvcResult created = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.latitude").value(45.0))
                .andExpect(jsonPath("$.longitude").value(9.0))
                .andReturn();

        JsonNode response = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = response.get("id").asText();

        mockMvc.perform(get("/cpos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.externalId").doesNotExist());
    }

    @ParameterizedTest(name = "CPO rejects {0}")
    @MethodSource("invalidCpoRequests")
    void rejectsCposWithInvalidRequiredFields(String description, String body, String externalId)
            throws Exception {
        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertCpoExternalIdIsAbsent(externalId);
    }

    @ParameterizedTest(name = "CPO rejects invalid {0}")
    @MethodSource("invalidCoordinates")
    void rejectsCposWithOutOfRangeCoordinates(String coordinate, String latitude, String longitude,
            String expectedError, String externalId) throws Exception {
        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid coordinate CPO","latitude":%s,"longitude":%s,
                                 "externalId":"%s"}
                                """.formatted(latitude, longitude, externalId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString(expectedError)));

        assertCpoExternalIdIsAbsent(externalId);
    }

    @Test
    void rejectsDuplicateCpoExternalIdsWithoutPersistingASecondResource() throws Exception {
        String externalId = unique("CPO-DUP");
        String firstId = createCpo(unique("First CPO"), externalId);

        mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cpoRequest(unique("Duplicate CPO"), externalId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Cpo externalId already in use")));

        mockMvc.perform(get("/cpos").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(firstId));
    }

    @Test
    void trimsCpoExternalIdsAndReturnsAnEmptyListForAnUnknownExternalId() throws Exception {
        String externalId = unique("CPO-TRIM");
        String id = createCpo(unique("Trimmed CPO"), "  " + externalId + "  ");

        mockMvc.perform(get("/cpos").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].externalId").value(externalId));

        mockMvc.perform(get("/cpos").param("externalId", unique("CPO-UNKNOWN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest(name = "eMSP externalId {0} is accepted and normalized")
    @MethodSource("optionalEmspExternalIds")
    void createsEmspsWithAnAbsentOrBlankExternalId(String description, String externalIdField)
            throws Exception {
        String name = unique("eMSP");
        String body = """
                {"name":"%s"%s}
                """.formatted(name, externalIdField);

        MvcResult created = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();

        JsonNode response = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = response.get("id").asText();

        mockMvc.perform(get("/emsps/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.externalId").doesNotExist());
    }

    @ParameterizedTest(name = "eMSP rejects {0}")
    @MethodSource("invalidEmspRequests")
    void rejectsEmspsWithAnInvalidName(String description, String body, String externalId)
            throws Exception {
        mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEmspExternalIdIsAbsent(externalId);
    }

    @Test
    void rejectsDuplicateEmspExternalIdsWithoutPersistingASecondResource() throws Exception {
        String externalId = unique("EMSP-DUP");
        String firstId = createEmsp(unique("First eMSP"), externalId);

        mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emspRequest(unique("Duplicate eMSP"), externalId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Emsp externalId already in use")));

        mockMvc.perform(get("/emsps").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(firstId));
    }

    @Test
    void searchesEmspsByExternalIdAndNameAndListsPersistedResources() throws Exception {
        String externalId = unique("EMSP-SEARCH");
        String firstName = unique("Mobility");
        String firstId = createEmsp(firstName, "  " + externalId + "  ");
        String secondId = createEmsp(unique("Other Provider"), unique("EMSP-OTHER"));

        mockMvc.perform(get("/emsps").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(firstId))
                .andExpect(jsonPath("$[0].externalId").value(externalId));

        mockMvc.perform(get("/emsps").param("name", firstName.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(firstId));

        mockMvc.perform(get("/emsps")
                        .param("externalId", externalId)
                        .param("name", "no matching name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/emsps").param("externalId", unique("EMSP-UNKNOWN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/emsps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItems(firstId, secondId)));
    }

    @Test
    void allowsTheSameExternalIdForDifferentResourceTypes() throws Exception {
        String externalId = unique("SHARED-EXTERNAL");

        createCpo(unique("Shared CPO"), externalId);
        createEmsp(unique("Shared eMSP"), externalId);

        mockMvc.perform(get("/cpos").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/emsps").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void returnsNotFoundForUnknownCpoAndEmspIds() throws Exception {
        String cpoId = UUID.randomUUID().toString();
        String emspId = UUID.randomUUID().toString();

        mockMvc.perform(get("/cpos/{id}", cpoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cpo not found: " + cpoId));

        mockMvc.perform(get("/emsps/{id}", emspId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Emsp not found: " + emspId));
    }

    private String createCpo(String name, String externalId) throws Exception {
        MvcResult result = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cpoRequest(name, externalId)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createEmsp(String name, String externalId) throws Exception {
        MvcResult result = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emspRequest(name, externalId)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String cpoRequest(String name, String externalId) {
        return """
                {"name":"%s","latitude":45.0,"longitude":9.0,"externalId":"%s"}
                """.formatted(name, externalId);
    }

    private String emspRequest(String name, String externalId) {
        return """
                {"name":"%s","externalId":"%s"}
                """.formatted(name, externalId);
    }

    private void assertCpoExternalIdIsAbsent(String externalId) throws Exception {
        mockMvc.perform(get("/cpos").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void assertEmspExternalIdIsAbsent(String externalId) throws Exception {
        mockMvc.perform(get("/emsps").param("externalId", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private static Stream<Arguments> optionalCpoExternalIds() {
        return Stream.of(
                Arguments.of("omitted", ""),
                Arguments.of("null", ",\"externalId\":null"),
                Arguments.of("empty", ",\"externalId\":\"\""),
                Arguments.of("whitespace", ",\"externalId\":\"   \""));
    }

    private static Stream<Arguments> invalidCpoRequests() {
        String missingNameId = unique("CPO-NO-NAME");
        String blankNameId = unique("CPO-BLANK-NAME");
        String missingLatitudeId = unique("CPO-NO-LAT");
        String missingLongitudeId = unique("CPO-NO-LON");
        return Stream.of(
                Arguments.of("missing name", """
                        {"name":null,"latitude":45.0,"longitude":9.0,"externalId":"%s"}
                        """.formatted(missingNameId), missingNameId),
                Arguments.of("blank name", """
                        {"name":"   ","latitude":45.0,"longitude":9.0,"externalId":"%s"}
                        """.formatted(blankNameId), blankNameId),
                Arguments.of("missing latitude", """
                        {"name":"Invalid CPO","latitude":null,"longitude":9.0,"externalId":"%s"}
                        """.formatted(missingLatitudeId), missingLatitudeId),
                Arguments.of("missing longitude", """
                        {"name":"Invalid CPO","latitude":45.0,"longitude":null,"externalId":"%s"}
                        """.formatted(missingLongitudeId), missingLongitudeId));
    }

    private static Stream<Arguments> invalidCoordinates() {
        return Stream.of(
                Arguments.of("latitude", "91.0", "9.0", "Latitude", unique("CPO-BAD-LAT")),
                Arguments.of("longitude", "45.0", "181.0", "Longitude", unique("CPO-BAD-LON")));
    }

    private static Stream<Arguments> optionalEmspExternalIds() {
        return Stream.of(
                Arguments.of("omitted", ""),
                Arguments.of("null", ",\"externalId\":null"),
                Arguments.of("empty", ",\"externalId\":\"\""),
                Arguments.of("whitespace", ",\"externalId\":\"   \""));
    }

    private static Stream<Arguments> invalidEmspRequests() {
        String missingNameId = unique("EMSP-NO-NAME");
        String blankNameId = unique("EMSP-BLANK-NAME");
        return Stream.of(
                Arguments.of("missing name", """
                        {"name":null,"externalId":"%s"}
                        """.formatted(missingNameId), missingNameId),
                Arguments.of("blank name", """
                        {"name":"   ","externalId":"%s"}
                        """.formatted(blankNameId), blankNameId));
    }
}
