package com.sch.energyBrokerService.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(EmspRepositoryAdapter.class)
class EmspRepositoryAdapterTest {

    @Autowired
    private EmspRepositoryAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsAnEmspByItsExternalId() {
        Emsp saved = adapter.save(new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa"));

        assertThat(adapter.findByExternalId("EMSP-001"))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.id()).isEqualTo(saved.id()));
    }

    @Test
    void allowsSeveralEmspsWithoutExternalId() {
        adapter.save(new Emsp(EmspId.newId(), null, "Mobility Spa"));
        adapter.save(new Emsp(EmspId.newId(), null, "Charge Now"));
        entityManager.flush();

        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    void deletesAnEmspById() {
        Emsp saved = adapter.save(new Emsp(EmspId.newId(), null, "Mobility Spa"));
        entityManager.flush();

        adapter.deleteById(saved.id());
        entityManager.flush();
        entityManager.clear();

        assertThat(adapter.findById(saved.id())).isEmpty();
    }

    @Test
    void enforcesTheUniquenessOfTheExternalId() {
        adapter.save(new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa"));
        entityManager.flush();

        adapter.save(new Emsp(EmspId.newId(), "EMSP-001", "Charge Now"));

        assertThatThrownBy(entityManager::flush).isInstanceOf(Exception.class);
    }
}
