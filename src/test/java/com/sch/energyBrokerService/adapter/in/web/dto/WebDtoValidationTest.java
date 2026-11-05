package com.sch.energyBrokerService.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.PriceQuoteRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.TariffPayload;
import com.sch.energyBrokerService.adapter.in.web.dto.Request.UpdateTariffRequest;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class WebDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createCpoRequestRejectsMissingRequiredFields() {
        Set<ConstraintViolation<CreateCpoRequest>> violations = validator.validate(
                new CreateCpoRequest(" ", null, null, "CPO-001"));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Cpo name is required", "Cpo latitude is required", "Cpo longitude is required");
    }

    @Test
    void createEmspRequestRejectsEmptyName() {
        Set<ConstraintViolation<CreateEmspRequest>> violations = validator.validate(
                new CreateEmspRequest("   ", "EMSP-001"));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Emsp name is required");
    }

    @Test
    void tariffPayloadRejectsMissingTypeAndNonPositivePrice() {
        Set<ConstraintViolation<TariffPayload>> violations = validator.validate(
                new TariffPayload(null, BigDecimal.ZERO));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Tariff type is required", "Tariff pricePerKwh must be positive");
    }

    @Test
    void priceQuoteRequestRejectsBlankNameAndNonPositiveEnergy() {
        Set<ConstraintViolation<PriceQuoteRequest>> violations = validator.validate(
                new PriceQuoteRequest("   ", BigDecimal.ZERO));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Cpo name is required", "kWh must be positive");
    }

    @Test
    void agreementRequestRequiresCpoIdEmspIdAndTariff() {
        Set<ConstraintViolation<CreateAgreementRequest>> violations = validator.validate(
                new CreateAgreementRequest(null, null, null));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("cpoId is required", "emspId is required", "tariff is required");
    }

    @Test
    void updateTariffRequestRequiresTariffPayload() {
        Set<ConstraintViolation<UpdateTariffRequest>> violations = validator.validate(
                new UpdateTariffRequest(null));

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("tariff is required");
    }

    @Test
    void validRequestsPassValidation() {
        CreateCpoRequest cpoRequest = new CreateCpoRequest("Charge Spa", 45.464211, 9.191383, "CPO-001");
        CreateEmspRequest emspRequest = new CreateEmspRequest("Mobility Spa", "EMSP-001");
        TariffPayload tariff = new TariffPayload(TariffType.FLAT, new BigDecimal("0.25"));
        PriceQuoteRequest quote = new PriceQuoteRequest("Charge Spa", new BigDecimal("30.5"));
        CreateAgreementRequest agreement = new CreateAgreementRequest(UUID.randomUUID(), UUID.randomUUID(), tariff);
        UpdateTariffRequest update = new UpdateTariffRequest(tariff);

        assertThat(validator.validate(cpoRequest)).isEmpty();
        assertThat(validator.validate(emspRequest)).isEmpty();
        assertThat(validator.validate(tariff)).isEmpty();
        assertThat(validator.validate(quote)).isEmpty();
        assertThat(validator.validate(agreement)).isEmpty();
        assertThat(validator.validate(update)).isEmpty();
    }
}
