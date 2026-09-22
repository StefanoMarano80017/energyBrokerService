package com.sch.energyBrokerService.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(CpoRepositoryAdapter.class)
class CpoRepositoryAdapterTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    @Autowired
    private CpoRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsACpoByItsExternalId() {
        Cpo saved = adapter.save(new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN));

        assertThat(adapter.findByExternalId("CPO-001"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.id()).isEqualTo(saved.id());
                    assertThat(found.name()).isEqualTo("Charge Spa");
                });
    }

    @Test
    void returnsEmptyForAnUnknownExternalId() {
        adapter.save(new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN));

        assertThat(adapter.findByExternalId("CPO-999")).isEmpty();
    }

    @Test
    void allowsSeveralCposWithoutExternalId() {
        adapter.save(new Cpo(CpoId.newId(), null, "Charge Spa", MILAN));
        adapter.save(new Cpo(CpoId.newId(), null, "Power Srl", MILAN));
        entityManager.flush();

        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    void enforcesTheUniquenessOfTheExternalId() {
        adapter.save(new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN));
        entityManager.flush();

        adapter.save(new Cpo(CpoId.newId(), "CPO-001", "Power Srl", MILAN));

        assertThatThrownBy(entityManager::flush).isInstanceOf(Exception.class);
    }
}
