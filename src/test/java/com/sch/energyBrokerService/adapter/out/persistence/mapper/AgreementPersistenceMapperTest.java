package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.out.persistence.entity.AgreementEntity;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

class AgreementPersistenceMapperTest {

    @Test
    void mapsAgreementToEntityAndBackWithoutLosingValues() {
        Instant createdAt = Instant.parse("2025-01-02T03:04:05Z");
        Instant acceptedAt = Instant.parse("2025-01-03T03:04:05Z");
        Instant lastModifiedAt = Instant.parse("2025-01-04T03:04:05Z");
        Agreement agreement = new Agreement(
                AgreementId.newId(),
                CpoId.newId(),
                EmspId.newId(),
                new Tariff(TariffId.newId(), TariffType.DYNAMIC, new PricePerKwh(new BigDecimal("0.4567"))),
                AgreementStatus.ACCEPTED,
                createdAt,
                acceptedAt,
                lastModifiedAt);

        AgreementEntity entity = AgreementPersistenceMapper.toEntity(agreement);
        Agreement restored = AgreementPersistenceMapper.toDomain(entity);

        assertThat(restored.id()).isEqualTo(agreement.id());
        assertThat(restored.cpoId()).isEqualTo(agreement.cpoId());
        assertThat(restored.emspId()).isEqualTo(agreement.emspId());
        assertThat(restored.tariff().id()).isEqualTo(agreement.tariff().id());
        assertThat(restored.tariff().type()).isEqualTo(TariffType.DYNAMIC);
        assertThat(restored.tariff().pricePerKwh().amount()).isEqualByComparingTo("0.4567");
        assertThat(restored.status()).isEqualTo(AgreementStatus.ACCEPTED);
        assertThat(restored.createdAt()).isEqualTo(createdAt);
        assertThat(restored.acceptedAt()).isEqualTo(acceptedAt);
        assertThat(restored.lastModifiedAt()).isEqualTo(lastModifiedAt);
    }

    @Test
    void mapsAnAgreementToItsResponse() {
        Agreement agreement = Agreement.propose(
                CpoId.newId(),
                EmspId.newId(),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))));

        var response = AgreementPersistenceMapper.toResponse(agreement);

        assertThat(response.id()).isEqualTo(agreement.id().value());
        assertThat(response.cpoId()).isEqualTo(agreement.cpoId().value());
        assertThat(response.emspId()).isEqualTo(agreement.emspId().value());
        assertThat(response.tariff().tariffId()).isEqualTo(agreement.tariff().id().value());
        assertThat(response.tariff().type()).isEqualTo("FLAT");
        assertThat(response.tariff().pricePerKwh()).isEqualByComparingTo("0.4200");
        assertThat(response.status()).isEqualTo("PROPOSED");
    }

    @Test
    void preservesOptionalNullTimestampsForAProposedAgreement() {
        Agreement agreement = new Agreement(
                AgreementId.newId(),
                CpoId.newId(),
                EmspId.newId(),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))),
                AgreementStatus.PROPOSED,
                Instant.parse("2025-01-02T03:04:05Z"),
                null,
                null);

        AgreementEntity entity = AgreementPersistenceMapper.toEntity(agreement);
        Agreement restored = AgreementPersistenceMapper.toDomain(entity);
        var response = AgreementPersistenceMapper.toResponse(agreement);

        assertThat(entity.getAcceptedAt()).isNull();
        assertThat(entity.getLastModifiedAt()).isNull();
        assertThat(restored.acceptedAt()).isNull();
        assertThat(restored.lastModifiedAt()).isNull();
        assertThat(response.acceptedAt()).isNull();
        assertThat(response.lastModifiedAt()).isNull();
    }
}
