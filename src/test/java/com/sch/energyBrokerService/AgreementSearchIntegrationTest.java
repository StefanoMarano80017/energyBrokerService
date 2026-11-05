package com.sch.energyBrokerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:agreement-search;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class AgreementSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabaseBeforeTest() {
        clearDatabase();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        clearDatabase();
    }

    private void clearDatabase() {
        jdbcTemplate.update("delete from agreements");
        jdbcTemplate.update("delete from cpos");
        jdbcTemplate.update("delete from emsps");
    }

    @Test
    void returnsAnEmptyListWhenNoAgreementsExist() throws Exception {
        assertSearchReturns(Map.of(), Set.of());
    }

    @ParameterizedTest(name = "empty database returns no Agreements for status {0}")
    @MethodSource("statusSearchCases")
    void returnsNoAgreementsForAValidStatusWhenDatabaseIsEmpty(String status, String ignored)
            throws Exception {
        assertSearchReturns(Map.of("status", status), Set.of());
    }

    @Test
    void returnsAllAgreementsWhenNoFiltersAreProvided() throws Exception {
        SearchDataset dataset = createDataset();

        assertSearchReturns(Map.of(), dataset.allAgreementIds());
    }

    @ParameterizedTest(name = "CPO search: {0}")
    @MethodSource("cpoSearchCases")
    void filtersAgreementsBySingleCpoId(String scenario, String cpoKey, boolean unknown,
            String expectedKey) throws Exception {
        SearchDataset dataset = createDataset();
        UUID cpoId = unknown ? UUID.randomUUID() : dataset.cpo(cpoKey);
        Set<String> expected = expectedKey == null ? Set.of() : dataset.agreementsFor(expectedKey);

        assertSearchReturns(Map.of("cpoId", cpoId.toString()), expected);
    }

    @ParameterizedTest(name = "eMSP search: {0}")
    @MethodSource("emspSearchCases")
    void filtersAgreementsBySingleEmspId(String scenario, String emspKey, boolean unknown,
            String expectedKey) throws Exception {
        SearchDataset dataset = createDataset();
        UUID emspId = unknown ? UUID.randomUUID() : dataset.emsp(emspKey);
        Set<String> expected = expectedKey == null ? Set.of() : dataset.agreementsFor(expectedKey);

        assertSearchReturns(Map.of("emspId", emspId.toString()), expected);
    }

    @ParameterizedTest(name = "status search: {0}")
    @MethodSource("statusSearchCases")
    void filtersAgreementsByEachSupportedStatus(String status, String expectedKey) throws Exception {
        SearchDataset dataset = createDataset();

        assertSearchReturns(Map.of("status", status), dataset.agreementsFor(expectedKey));
    }

    @ParameterizedTest(name = "combined filters: {0}")
    @MethodSource("combinedSearchCases")
    void combinesSearchFiltersWithAndSemantics(
            String scenario,
            String cpoKey,
            String emspKey,
            String status,
            String expectedKey) throws Exception {
        SearchDataset dataset = createDataset();
        Map<String, String> filters = new java.util.LinkedHashMap<>();
        if (cpoKey != null) {
            filters.put("cpoId", dataset.cpo(cpoKey).toString());
        }
        if (emspKey != null) {
            filters.put("emspId", dataset.emsp(emspKey).toString());
        }
        if (status != null) {
            filters.put("status", status);
        }

        assertSearchReturns(filters, dataset.agreementsFor(expectedKey));
    }

    @ParameterizedTest(name = "invalid search parameter {0}={1}")
    @MethodSource("invalidSearchParameters")
    void rejectsMalformedFilterValues(String parameter, String value) throws Exception {
        mockMvc.perform(get("/agreements").param(parameter, value))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "whitespace {0} is treated as an absent filter")
    @MethodSource("whitespaceSearchParameters")
    void treatsWhitespaceFilterValuesAsAbsent(String parameter) throws Exception {
        SearchDataset dataset = createDataset();

        assertSearchReturns(Map.of(parameter, " "), dataset.allAgreementIds());
    }

    @Test
    void treatsEmptyFilterValuesAsAbsent() throws Exception {
        SearchDataset dataset = createDataset();

        assertSearchReturns(
                Map.of("cpoId", "", "emspId", "", "status", ""),
                dataset.allAgreementIds());
    }

    private void assertSearchReturns(Map<String, String> filters, Set<String> expectedIds) throws Exception {
        MockHttpServletRequestBuilder request = get("/agreements");
        filters.forEach((key, value) -> request.param(key, value));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        Set<String> actualIds = new HashSet<>();
        response.forEach(agreement -> actualIds.add(agreement.get("id").asText()));

        assertThat(response.isArray()).isTrue();
        assertThat(actualIds).containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    private SearchDataset createDataset() throws Exception {
        UUID cpoA = createCpo();
        UUID cpoB = createCpo();
        UUID cpoWithoutAgreements = createCpo();
        UUID emspX = createEmsp();
        UUID emspY = createEmsp();
        UUID emspWithoutAgreements = createEmsp();

        SearchDataset dataset = new SearchDataset(cpoA, cpoB, cpoWithoutAgreements,
                emspX, emspY, emspWithoutAgreements);

        dataset.add("axProposed", publishAgreement(cpoA, emspX));
        dataset.add("axAccepted", accept(publishAgreement(cpoA, emspX)));
        dataset.add("ayAccepted", accept(publishAgreement(cpoA, emspY)));
        dataset.add("bxRejected", reject(publishAgreement(cpoB, emspX)));
        dataset.add("byCancelled", cancel(publishAgreement(cpoB, emspY)));
        return dataset;
    }

    private UUID createCpo() throws Exception {
        MvcResult result = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Search CPO %s","latitude":45.0,"longitude":9.0}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    private UUID createEmsp() throws Exception {
        MvcResult result = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Search eMSP %s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    private String publishAgreement(UUID cpoId, UUID emspId) throws Exception {
        MvcResult result = mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s",
                                 "tariff":{"type":"FLAT","pricePerKwh":0.25}}
                                """.formatted(cpoId, emspId)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String accept(String agreementId) throws Exception {
        return transition(agreementId, "accept");
    }

    private String reject(String agreementId) throws Exception {
        return transition(agreementId, "reject");
    }

    private String cancel(String agreementId) throws Exception {
        return transition(agreementId, "cancel");
    }

    private String transition(String agreementId, String operation) throws Exception {
        MvcResult result = mockMvc.perform(post("/agreements/{id}/{operation}", agreementId, operation))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private static Stream<Arguments> cpoSearchCases() {
        return Stream.of(
                Arguments.of("one CPO with multiple Agreements", "cpoA", false, "cpoA"),
                Arguments.of("another CPO", "cpoB", false, "cpoB"),
                Arguments.of("CPO without Agreements", "cpoEmpty", false, null),
                Arguments.of("unknown CPO UUID", "unused", true, null));
    }

    private static Stream<Arguments> emspSearchCases() {
        return Stream.of(
                Arguments.of("one eMSP with Agreements across CPOs", "emspX", false, "emspX"),
                Arguments.of("another eMSP", "emspY", false, "emspY"),
                Arguments.of("eMSP without Agreements", "emspEmpty", false, null),
                Arguments.of("unknown eMSP UUID", "unused", true, null));
    }

    private static Stream<Arguments> statusSearchCases() {
        return Arrays.stream(AgreementStatus.values())
                .map(status -> Arguments.of(status.name(), status.name()));
    }

    private static Stream<Arguments> combinedSearchCases() {
        return Stream.of(
                Arguments.of("CPO + eMSP", "cpoA", "emspX", null, "pairAx"),
                Arguments.of("CPO + status", "cpoA", null, "ACCEPTED", "cpoAAccepted"),
                Arguments.of("eMSP + status", null, "emspX", "ACCEPTED", "emspXAccepted"),
                Arguments.of("CPO + eMSP + status", "cpoA", "emspX", "ACCEPTED", "tripleAxAccepted"));
    }

    private static Stream<Arguments> invalidSearchParameters() {
        return Stream.of(
                Arguments.of("cpoId", "not-a-uuid"),
                Arguments.of("emspId", "not-a-uuid"),
                Arguments.of("status", "UNKNOWN"));
    }

    private static Stream<Arguments> whitespaceSearchParameters() {
        return Stream.of(
                Arguments.of("cpoId"),
                Arguments.of("emspId"),
                Arguments.of("status"));
    }

    private static final class SearchDataset {

        private final UUID cpoA;
        private final UUID cpoB;
        private final UUID cpoEmpty;
        private final UUID emspX;
        private final UUID emspY;
        private final UUID emspEmpty;
        private final Map<String, Set<String>> agreementGroups = new java.util.HashMap<>();
        private final Set<String> allAgreementIds = new HashSet<>();

        private SearchDataset(UUID cpoA, UUID cpoB, UUID cpoEmpty,
                UUID emspX, UUID emspY, UUID emspEmpty) {
            this.cpoA = cpoA;
            this.cpoB = cpoB;
            this.cpoEmpty = cpoEmpty;
            this.emspX = emspX;
            this.emspY = emspY;
            this.emspEmpty = emspEmpty;
        }

        private UUID cpo(String key) {
            return switch (key) {
                case "cpoA" -> cpoA;
                case "cpoB" -> cpoB;
                case "cpoEmpty" -> cpoEmpty;
                default -> throw new IllegalArgumentException("Unknown CPO dataset key: " + key);
            };
        }

        private UUID emsp(String key) {
            return switch (key) {
                case "emspX" -> emspX;
                case "emspY" -> emspY;
                case "emspEmpty" -> emspEmpty;
                default -> throw new IllegalArgumentException("Unknown eMSP dataset key: " + key);
            };
        }

        private void add(String key, String agreementId) {
            allAgreementIds.add(agreementId);
            AgreementStatus status = switch (key) {
                case "axProposed" -> AgreementStatus.PROPOSED;
                case "axAccepted", "ayAccepted" -> AgreementStatus.ACCEPTED;
                case "bxRejected" -> AgreementStatus.REJECTED;
                case "byCancelled" -> AgreementStatus.CANCELLED;
                default -> throw new IllegalArgumentException("Unknown Agreement dataset key: " + key);
            };
            agreementGroups.computeIfAbsent(status.name(), ignored -> new HashSet<>()).add(agreementId);
            agreementGroups.computeIfAbsent("cpoA", ignored -> new HashSet<>());
            agreementGroups.computeIfAbsent("cpoB", ignored -> new HashSet<>());
            agreementGroups.computeIfAbsent("emspX", ignored -> new HashSet<>());
            agreementGroups.computeIfAbsent("emspY", ignored -> new HashSet<>());
            if (key.startsWith("ax") || key.startsWith("ay")) {
                agreementGroups.get("cpoA").add(agreementId);
            } else {
                agreementGroups.get("cpoB").add(agreementId);
            }
            if (key.endsWith("xProposed") || key.endsWith("xAccepted") || key.endsWith("xRejected")) {
                agreementGroups.get("emspX").add(agreementId);
            } else {
                agreementGroups.get("emspY").add(agreementId);
            }

            if (key.equals("axAccepted")) {
                agreementGroups.computeIfAbsent("pairAx", ignored -> new HashSet<>()).add(agreementId);
                agreementGroups.computeIfAbsent("emspXAccepted", ignored -> new HashSet<>()).add(agreementId);
                agreementGroups.computeIfAbsent("tripleAxAccepted", ignored -> new HashSet<>()).add(agreementId);
            }
            if (key.equals("axAccepted") || key.equals("ayAccepted")) {
                agreementGroups.computeIfAbsent("cpoAAccepted", ignored -> new HashSet<>()).add(agreementId);
            }
            if (key.equals("axProposed")) {
                agreementGroups.computeIfAbsent("pairAx", ignored -> new HashSet<>()).add(agreementId);
            }
        }

        private Set<String> allAgreementIds() {
            return Set.copyOf(allAgreementIds);
        }

        private Set<String> agreementsFor(String key) {
            return Set.copyOf(agreementGroups.getOrDefault(key, Set.of()));
        }
    }
}
