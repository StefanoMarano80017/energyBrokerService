package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.adapter.out.persistence.entity.EmspEntity;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

class EmspPersistenceMapperTest {

    @Test
    void roundTripsTheExternalId() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");

        EmspEntity entity = EmspPersistenceMapper.toEntity(emsp);
        assertThat(entity.getExternalId()).isEqualTo("EMSP-001");

        Emsp back = EmspPersistenceMapper.toDomain(entity);
        assertThat(back.externalId()).isEqualTo("EMSP-001");
        assertThat(back.id()).isEqualTo(emsp.id());
    }

    @Test
    void roundTripsAnEmspWithoutExternalId() {
        Emsp emsp = new Emsp(EmspId.newId(), null, "Mobility Spa");

        EmspEntity entity = EmspPersistenceMapper.toEntity(emsp);
        assertThat(entity.getExternalId()).isNull();
        assertThat(EmspPersistenceMapper.toDomain(entity).externalId()).isNull();
    }

    @Test
    void exposesTheExternalIdInTheResponse() {
        Emsp emsp = new Emsp(EmspId.newId(), "EMSP-001", "Mobility Spa");

        var response = EmspPersistenceMapper.toResponse(emsp);

        assertThat(response.id()).isEqualTo(emsp.id().value());
        assertThat(response.externalId()).isEqualTo("EMSP-001");
        assertThat(response.name()).isEqualTo("Mobility Spa");
    }
}
