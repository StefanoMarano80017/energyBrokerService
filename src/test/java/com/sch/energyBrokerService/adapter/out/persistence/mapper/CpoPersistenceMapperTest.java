package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.out.persistence.entity.CpoEntity;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

class CpoPersistenceMapperTest {

    private static final Coordinate MILAN = new Coordinate(45.464211, 9.191383);

    @Test
    void roundTripsTheExternalId() {
        Cpo cpo = new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN);

        CpoEntity entity = CpoPersistenceMapper.toEntity(cpo);
        assertThat(entity.getExternalId()).isEqualTo("CPO-001");

        Cpo back = CpoPersistenceMapper.toDomain(entity);
        assertThat(back.externalId()).isEqualTo("CPO-001");
        assertThat(back.id()).isEqualTo(cpo.id());
        assertThat(back.name()).isEqualTo("Charge Spa");
    }

    @Test
    void roundTripsACpoWithoutExternalId() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", MILAN);

        CpoEntity entity = CpoPersistenceMapper.toEntity(cpo);
        assertThat(entity.getExternalId()).isNull();
        assertThat(CpoPersistenceMapper.toDomain(entity).externalId()).isNull();
    }

    @Test
    void exposesTheExternalIdInTheResponse() {
        Cpo cpo = new Cpo(CpoId.newId(), "CPO-001", "Charge Spa", MILAN);

        var response = CpoPersistenceMapper.toResponse(cpo);

        assertThat(response.id()).isEqualTo(cpo.id().value());
        assertThat(response.externalId()).isEqualTo("CPO-001");
        assertThat(response.name()).isEqualTo("Charge Spa");
        assertThat(response.latitude()).isEqualTo(45.464211);
        assertThat(response.longitude()).isEqualTo(9.191383);
    }

    @Test
    void exposesANullExternalIdInTheResponse() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", MILAN);

        assertThat(CpoPersistenceMapper.toResponse(cpo).externalId()).isNull();
    }
}
