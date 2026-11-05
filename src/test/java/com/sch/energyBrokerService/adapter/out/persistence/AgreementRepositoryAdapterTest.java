package com.sch.energyBrokerService.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(AgreementRepositoryAdapter.class)
class AgreementRepositoryAdapterTest {

    @Autowired
    private AgreementRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void returnsAllAgreementsWhenNoSearchFiltersAreProvided() {
        Agreement first = saveAgreement(UUID.randomUUID(), UUID.randomUUID(), false);
        Agreement second = saveAgreement(UUID.randomUUID(), UUID.randomUUID(), true);
        entityManager.flush();

        assertThat(adapter.search(null, null, null))
                .extracting(Agreement::id)
                .containsExactlyInAnyOrder(first.id(), second.id());
    }

    @Test
    void filtersAgreementsByCpo() {
        UUID matchingCpoId = UUID.randomUUID();
        Agreement first = saveAgreement(matchingCpoId, UUID.randomUUID(), false);
        Agreement second = saveAgreement(matchingCpoId, UUID.randomUUID(), true);
        saveAgreement(UUID.randomUUID(), UUID.randomUUID(), false);
        entityManager.flush();

        assertThat(adapter.search(new CpoId(matchingCpoId), null, null))
                .extracting(Agreement::id)
                .containsExactlyInAnyOrder(first.id(), second.id());
    }

    @Test
    void combinesCpoEmspAndStatusFilters() {
        UUID cpoId = UUID.randomUUID();
        UUID emspId = UUID.randomUUID();
        Agreement matching = saveAgreement(cpoId, emspId, true);
        saveAgreement(cpoId, UUID.randomUUID(), true);
        saveAgreement(UUID.randomUUID(), emspId, true);
        saveAgreement(cpoId, emspId, false);
        entityManager.flush();

        assertThat(adapter.search(new CpoId(cpoId), new EmspId(emspId), AgreementStatus.ACCEPTED))
                .extracting(Agreement::id)
                .containsExactly(matching.id());
    }

    @Test
    void returnsNoAgreementsWhenTheSearchCriteriaDoNotMatch() {
        saveAgreement(UUID.randomUUID(), UUID.randomUUID(), false);
        entityManager.flush();

        assertThat(adapter.search(
                new CpoId(UUID.randomUUID()),
                new EmspId(UUID.randomUUID()),
                AgreementStatus.ACCEPTED))
                .isEmpty();
    }

    private Agreement saveAgreement(UUID cpoId, UUID emspId, boolean accepted) {
        Agreement agreement = Agreement.propose(
                new CpoId(cpoId),
                new EmspId(emspId),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.25"))));
        if (accepted) {
            agreement.accept();
        }
        return adapter.save(agreement);
    }
}
