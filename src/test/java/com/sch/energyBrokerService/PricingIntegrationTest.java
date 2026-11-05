package com.sch.energyBrokerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pricing-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class PricingIntegrationTest {

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

    @ParameterizedTest(name = "quote uses persisted {0} tariff")
    @MethodSource("tariffScenarios")
    void recoversEachTariffTypeAndCalculatesItsQuote(TariffType type, String rate, String kwh, String expectedTotal)
            throws Exception {
        String cpoName = unique("Tariff CPO");
        UUID cpoId = createCpo(cpoName);
        UUID emspId = createEmsp();
        String agreementId = publishAgreement(cpoId, emspId, type, rate);
        acceptAgreement(agreementId);

        JsonNode quote = parseSingleQuote(requestQuote(cpoName, kwh)
                .andExpect(status().isOk())
                .andReturn());

        assertThat(quote.get("agreementId").asText()).isEqualTo(agreementId);
        assertThat(quote.get("cpoId").asText()).isEqualTo(cpoId.toString());
        assertThat(quote.get("emspId").asText()).isEqualTo(emspId.toString());
        assertThat(quote.get("type").asText()).isEqualTo(type.name());
        assertThat(new BigDecimal(quote.get("pricePerKwh").asText()))
                .isEqualByComparingTo(new BigDecimal(rate).setScale(4));
        assertThat(new BigDecimal(quote.get("kwh").asText())).isEqualByComparingTo(new BigDecimal(kwh));
        assertThat(new BigDecimal(quote.get("totalCost").asText()))
                .isEqualByComparingTo(new BigDecimal(expectedTotal));
        assertThat(quote.get("tariffId").asText()).isNotBlank();
    }

    @Test
    void returnsAnEmptyQuoteListWhenCpoHasNoAgreements() throws Exception {
        String cpoName = unique("No agreements CPO");
        createCpo(cpoName);

        JsonNode quotes = parseQuoteList(requestQuote(cpoName, "10")
                .andExpect(status().isOk())
                .andReturn());

        assertThat(quotes).isEmpty();
    }

    @ParameterizedTest(name = "Agreement in {0} state is not quoted")
    @MethodSource("nonAcceptedStates")
    void returnsNoQuoteWhenCpoHasOnlyNonAcceptedAgreements(String state, String operation) throws Exception {
        String cpoName = unique("Inactive CPO");
        UUID cpoId = createCpo(cpoName);
        UUID emspId = createEmsp();
        String agreementId = publishAgreement(cpoId, emspId, TariffType.FLAT, "0.50");
        if (!operation.isEmpty()) {
            transitionAgreement(agreementId, operation);
        }

        JsonNode quotes = parseQuoteList(requestQuote(cpoName, "10")
                .andExpect(status().isOk())
                .andReturn());

        assertThat(quotes).isEmpty();
    }

    @Test
    void includesEveryAcceptedAgreementAndEveryCpoWithTheSameName() throws Exception {
        String cpoName = unique("Shared CPO");
        UUID firstCpoId = createCpo(cpoName);
        UUID secondCpoId = createCpo(cpoName);
        UUID firstEmspId = createEmsp();
        UUID secondEmspId = createEmsp();

        String firstAgreementId = publishAgreement(firstCpoId, firstEmspId, TariffType.FLAT, "0.20");
        String secondAgreementId = publishAgreement(firstCpoId, secondEmspId, TariffType.DYNAMIC, "0.35");
        String thirdAgreementId = publishAgreement(secondCpoId, firstEmspId, TariffType.TIME_BASED, "0.47");
        acceptAgreement(firstAgreementId);
        acceptAgreement(secondAgreementId);
        acceptAgreement(thirdAgreementId);

        JsonNode quotes = parseQuoteList(requestQuote("  " + cpoName.toLowerCase() + "  ", "10")
                .andExpect(status().isOk())
                .andReturn());

        assertThat(quoteIds(quotes)).containsExactlyInAnyOrder(
                firstAgreementId, secondAgreementId, thirdAgreementId);
        Map<String, BigDecimal> totals = quoteTotalsByAgreement(quotes);
        assertThat(totals).hasSize(3);
        assertThat(totals.get(firstAgreementId)).isEqualByComparingTo("2.00");
        assertThat(totals.get(secondAgreementId)).isEqualByComparingTo("3.50");
        assertThat(totals.get(thirdAgreementId)).isEqualByComparingTo("4.70");
        assertThat(quoteCpoIds(quotes)).containsExactlyInAnyOrder(
                firstCpoId.toString(), firstCpoId.toString(), secondCpoId.toString());
    }

    @Test
    void returnsNotFoundWhenNoCpoMatchesTheRequestedName() throws Exception {
        String missingName = unique("Missing CPO");

        mockMvc.perform(post("/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quoteRequest(missingName, "10")))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "invalid pricing request: {0}")
    @MethodSource("invalidRequests")
    void rejectsInvalidQuoteRequests(String description, String body) throws Exception {
        mockMvc.perform(post("/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions requestQuote(String cpoName, String kwh)
            throws Exception {
        return mockMvc.perform(post("/pricing/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quoteRequest(cpoName, kwh)));
    }

    private UUID createCpo(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","latitude":45.0,"longitude":9.0}
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    private UUID createEmsp() throws Exception {
        MvcResult result = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pricing eMSP %s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText());
    }

    private String publishAgreement(UUID cpoId, UUID emspId, TariffType type, String price)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s",
                                 "tariff":{"type":"%s","pricePerKwh":%s}}
                                """.formatted(cpoId, emspId, type, price)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void acceptAgreement(String agreementId) throws Exception {
        transitionAgreement(agreementId, "accept");
    }

    private void transitionAgreement(String agreementId, String operation) throws Exception {
        mockMvc.perform(post("/agreements/{id}/{operation}", agreementId, operation))
                .andExpect(status().isOk());
    }

    private JsonNode parseSingleQuote(MvcResult result) throws Exception {
        JsonNode response = parseQuoteList(result);
        assertThat(response).hasSize(1);
        return response.get(0);
    }

    private JsonNode parseQuoteList(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, BigDecimal> quoteTotalsByAgreement(JsonNode quotes) {
        Map<String, BigDecimal> totals = new HashMap<>();
        quotes.forEach(quote -> totals.put(
                quote.get("agreementId").asText(),
                new BigDecimal(quote.get("totalCost").asText())));
        return totals;
    }

    private List<String> quoteIds(JsonNode quotes) {
        List<String> ids = new ArrayList<>();
        quotes.forEach(quote -> ids.add(quote.get("agreementId").asText()));
        return ids;
    }

    private List<String> quoteCpoIds(JsonNode quotes) {
        List<String> ids = new ArrayList<>();
        quotes.forEach(quote -> ids.add(quote.get("cpoId").asText()));
        return ids;
    }

    private void clearDatabase() {
        jdbcTemplate.update("delete from agreements");
        jdbcTemplate.update("delete from cpos");
        jdbcTemplate.update("delete from emsps");
    }

    private static String quoteRequest(String cpoName, String kwh) {
        return """
                {"cpoName":"%s","kwh":%s}
                """.formatted(cpoName, kwh);
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private static Stream<Arguments> tariffScenarios() {
        return Stream.of(
                Arguments.of(TariffType.FLAT, "0.42", "12.5", "5.25"),
                Arguments.of(TariffType.TIME_BASED, "0.31", "10", "3.10"),
                Arguments.of(TariffType.DYNAMIC, "0.45", "8", "3.60"));
    }

    private static Stream<Arguments> nonAcceptedStates() {
        return Stream.of(
                Arguments.of("PROPOSED", ""),
                Arguments.of("REJECTED", "reject"),
                Arguments.of("CANCELLED", "cancel"));
    }

    private static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of("missing CPO name", """
                        {"cpoName":null,"kwh":10}
                        """),
                Arguments.of("blank CPO name", """
                        {"cpoName":"   ","kwh":10}
                        """),
                Arguments.of("missing kWh", """
                        {"cpoName":"Charge Network","kwh":null}
                        """),
                Arguments.of("zero kWh", """
                        {"cpoName":"Charge Network","kwh":0}
                        """),
                Arguments.of("negative kWh", """
                        {"cpoName":"Charge Network","kwh":-1}
                        """));
    }
}
