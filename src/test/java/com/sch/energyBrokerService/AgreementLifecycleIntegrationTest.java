package com.sch.energyBrokerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

@SpringBootTest
@AutoConfigureMockMvc
class AgreementLifecycleIntegrationTest {

    private static final String INITIAL_TARIFF_TYPE = "FLAT";
    private static final String INITIAL_TARIFF_PRICE = "0.2500";
    private static final String UPDATED_TARIFF_TYPE = "TIME_BASED";
    private static final String UPDATED_TARIFF_PRICE = "0.3700";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publishesAnAgreementAndPersistsItsProposedStateAndTariff() throws Exception {
        JsonNode published = publishAgreement();

        assertThat(published.get("status").asText()).isEqualTo("PROPOSED");
        assertThat(published.get("acceptedAt").isNull()).isTrue();
        assertThat(published.get("tariff").get("type").asText()).isEqualTo(INITIAL_TARIFF_TYPE);
        assertThat(new BigDecimal(published.get("tariff").get("pricePerKwh").asText()))
                .isEqualByComparingTo(INITIAL_TARIFF_PRICE);

        JsonNode persisted = getAgreement(published.get("id").asText());
        assertThat(persisted.get("id")).isEqualTo(published.get("id"));
        assertThat(persisted.get("cpoId")).isEqualTo(published.get("cpoId"));
        assertThat(persisted.get("emspId")).isEqualTo(published.get("emspId"));
        assertThat(persisted.get("status")).isEqualTo(published.get("status"));
        assertThat(persisted.get("acceptedAt")).isEqualTo(published.get("acceptedAt"));
        assertThat(persisted.get("createdAt").isNull()).isFalse();
        assertThat(persisted.get("lastModifiedAt").isNull()).isFalse();
        assertThat(persisted.get("tariff")).isEqualTo(published.get("tariff"));
    }

    @ParameterizedTest(name = "{0} -> {2} via {1}")
    @MethodSource("allowedTransitions")
    void persistsEachAllowedLifecycleTransition(
            AgreementStatus initialStatus,
            String operation,
            AgreementStatus expectedStatus,
            boolean acceptedAtPresent) throws Exception {
        String agreementId = createAgreementInState(initialStatus);

        performLifecycleOperation(agreementId, operation)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expectedStatus.name()))
                .andExpect(acceptedAtPresent
                        ? jsonPath("$.acceptedAt").isNotEmpty()
                        : jsonPath("$.acceptedAt").doesNotExist());

        JsonNode persisted = getAgreement(agreementId);
        assertThat(persisted.get("status").asText()).isEqualTo(expectedStatus.name());
        assertThat(persisted.get("acceptedAt").isNull()).isEqualTo(!acceptedAtPresent);
    }

    @ParameterizedTest(name = "{0} rejects {1}")
    @MethodSource("disallowedTransitions")
    void rejectsDisallowedTransitionsWithoutChangingPersistedAgreement(
            AgreementStatus initialStatus,
            String operation,
            String expectedError) throws Exception {
        String agreementId = createAgreementInState(initialStatus);
        JsonNode before = getAgreement(agreementId);

        performLifecycleOperation(agreementId, operation)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString(expectedError)));

        JsonNode after = getAgreement(agreementId);
        assertThat(after.get("status").asText()).isEqualTo(before.get("status").asText());
        assertThat(after.get("acceptedAt")).isEqualTo(before.get("acceptedAt"));
        assertThat(after.get("lastModifiedAt")).isEqualTo(before.get("lastModifiedAt"));
        assertThat(after.get("tariff")).isEqualTo(before.get("tariff"));
    }

    @ParameterizedTest(name = "tariff can be updated while Agreement is {0}")
    @MethodSource("allStatuses")
    void updatesAndPersistsTariffWithoutChangingAgreementStatus(AgreementStatus initialStatus)
            throws Exception {
        String agreementId = createAgreementInState(initialStatus);
        JsonNode before = getAgreement(agreementId);
        String originalTariffId = before.get("tariff").get("tariffId").asText();

        mockMvc.perform(post("/agreements/{id}/update-tariff", agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTariffRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(initialStatus.name()))
                .andExpect(jsonPath("$.tariff.tariffId").value(originalTariffId))
                .andExpect(jsonPath("$.tariff.type").value(UPDATED_TARIFF_TYPE))
                .andExpect(jsonPath("$.tariff.pricePerKwh").value(0.37));

        JsonNode persisted = getAgreement(agreementId);
        assertThat(persisted.get("status").asText()).isEqualTo(initialStatus.name());
        assertThat(persisted.get("acceptedAt")).isEqualTo(before.get("acceptedAt"));
        assertThat(persisted.get("tariff").get("tariffId").asText()).isEqualTo(originalTariffId);
        assertThat(persisted.get("tariff").get("type").asText()).isEqualTo(UPDATED_TARIFF_TYPE);
        assertThat(new BigDecimal(persisted.get("tariff").get("pricePerKwh").asText()))
                .isEqualByComparingTo(UPDATED_TARIFF_PRICE);
    }

    @ParameterizedTest(name = "invalid tariff update: {0}")
    @MethodSource("invalidTariffUpdates")
    void rejectsInvalidTariffUpdatesWithoutChangingPersistedAgreement(String description, String body)
            throws Exception {
        String agreementId = createAgreementInState(AgreementStatus.PROPOSED);
        JsonNode before = getAgreement(agreementId);

        mockMvc.perform(post("/agreements/{id}/update-tariff", agreementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        JsonNode after = getAgreement(agreementId);
        assertThat(after.get("status")).isEqualTo(before.get("status"));
        assertThat(after.get("acceptedAt")).isEqualTo(before.get("acceptedAt"));
        assertThat(after.get("lastModifiedAt")).isEqualTo(before.get("lastModifiedAt"));
        assertThat(after.get("tariff")).isEqualTo(before.get("tariff"));
    }

    @Test
    void rejectsPublishingAnAgreementWithInvalidRequestData() throws Exception {
        UUID cpoId = UUID.fromString(createCpo());
        UUID emspId = UUID.fromString(createEmsp());

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s","tariff":null}
                                """.formatted(cpoId, emspId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s",
                                 "tariff":{"type":"FLAT","pricePerKwh":0}}
                                """.formatted(cpoId, emspId)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "publication rejects missing {0}")
    @MethodSource("missingParties")
    void returnsNotFoundWhenPublishingWithAMissingParty(String missingParty, String errorFragment)
            throws Exception {
        String cpoId = missingParty.equals("CPO") ? UUID.randomUUID().toString() : createCpo();
        String emspId = missingParty.equals("eMSP") ? UUID.randomUUID().toString() : createEmsp();

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s",
                                 "tariff":{"type":"FLAT","pricePerKwh":0.25}}
                                """.formatted(cpoId, emspId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(errorFragment + " not found")));
    }

    @ParameterizedTest(name = "{0} returns not found for an unknown Agreement")
    @MethodSource("lifecycleOperations")
    void returnsNotFoundWhenApplyingLifecycleOperationToUnknownAgreement(
            String operation,
            String tariffBody) throws Exception {
        String agreementId = UUID.randomUUID().toString();

        performLifecycleOperation(agreementId, operation, tariffBody)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Agreement not found: " + agreementId));
    }

    private JsonNode publishAgreement() throws Exception {
        String cpoId = createCpo();
        String emspId = createEmsp();
        MvcResult result = mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpoId":"%s","emspId":"%s",
                                 "tariff":{"type":"%s","pricePerKwh":0.25}}
                                """.formatted(cpoId, emspId, INITIAL_TARIFF_TYPE)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createAgreementInState(AgreementStatus state) throws Exception {
        JsonNode agreement = publishAgreement();
        String agreementId = agreement.get("id").asText();

        if (state != AgreementStatus.PROPOSED) {
            String operation = switch (state) {
                case ACCEPTED -> "accept";
                case REJECTED -> "reject";
                case CANCELLED -> "cancel";
                case PROPOSED -> throw new IllegalStateException("PROPOSED requires no transition");
            };
            performLifecycleOperation(agreementId, operation).andExpect(status().isOk());
        }
        return agreementId;
    }

    private String createCpo() throws Exception {
        MvcResult result = mockMvc.perform(post("/cpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lifecycle CPO %s","latitude":45.0,"longitude":9.0}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createEmsp() throws Exception {
        MvcResult result = mockMvc.perform(post("/emsps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lifecycle eMSP %s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private JsonNode getAgreement(String id) throws Exception {
        MvcResult result = mockMvc.perform(get("/agreements/{id}", id))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.ResultActions performLifecycleOperation(
            String agreementId,
            String operation) throws Exception {
        return performLifecycleOperation(agreementId, operation, updatedTariffRequest());
    }

    private org.springframework.test.web.servlet.ResultActions performLifecycleOperation(
            String agreementId,
            String operation,
            String tariffBody) throws Exception {
        MockHttpServletRequestBuilder request = post("/agreements/{id}/{operation}", agreementId, operation);
        if (operation.equals("update-tariff")) {
            request.contentType(MediaType.APPLICATION_JSON).content(tariffBody);
        }
        return mockMvc.perform(request);
    }

    private static String updatedTariffRequest() {
        return """
                {"tariff":{"type":"%s","pricePerKwh":0.37}}
                """.formatted(UPDATED_TARIFF_TYPE);
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(AgreementStatus.PROPOSED, "accept", AgreementStatus.ACCEPTED, true),
                Arguments.of(AgreementStatus.PROPOSED, "reject", AgreementStatus.REJECTED, false),
                Arguments.of(AgreementStatus.PROPOSED, "cancel", AgreementStatus.CANCELLED, false),
                Arguments.of(AgreementStatus.ACCEPTED, "cancel", AgreementStatus.CANCELLED, true));
    }

    private static Stream<Arguments> disallowedTransitions() {
        return Stream.of(
                Arguments.of(AgreementStatus.ACCEPTED, "accept", "Cannot accept"),
                Arguments.of(AgreementStatus.ACCEPTED, "reject", "Cannot reject"),
                Arguments.of(AgreementStatus.REJECTED, "accept", "Cannot accept"),
                Arguments.of(AgreementStatus.REJECTED, "reject", "Cannot reject"),
                Arguments.of(AgreementStatus.REJECTED, "cancel", "Cannot cancel"),
                Arguments.of(AgreementStatus.CANCELLED, "accept", "Cannot accept"),
                Arguments.of(AgreementStatus.CANCELLED, "reject", "Cannot reject"),
                Arguments.of(AgreementStatus.CANCELLED, "cancel", "Cannot cancel"));
    }

    private static Stream<Arguments> allStatuses() {
        return Stream.of(AgreementStatus.values()).map(Arguments::of);
    }

    private static Stream<Arguments> invalidTariffUpdates() {
        return Stream.of(
                Arguments.of("missing tariff", """
                        {"tariff":null}
                        """),
                Arguments.of("non-positive price", """
                        {"tariff":{"type":"FLAT","pricePerKwh":0}}
                        """));
    }

    private static Stream<Arguments> missingParties() {
        return Stream.of(
                Arguments.of("CPO", "Cpo"),
                Arguments.of("eMSP", "Emsp"));
    }

    private static Stream<Arguments> lifecycleOperations() {
        return Stream.of(
                Arguments.of("accept", updatedTariffRequest()),
                Arguments.of("reject", updatedTariffRequest()),
                Arguments.of("cancel", updatedTariffRequest()),
                Arguments.of("update-tariff", updatedTariffRequest()));
    }
}
