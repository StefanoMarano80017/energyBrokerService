package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.EmspResponse;
import com.sch.energyBrokerService.adapter.out.persistence.entity.EmspEntity;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

public class EmspPersistenceMapper {

    public static EmspEntity toEntity(Emsp emsp) {
        return EmspEntity.builder()
                .id(emsp.id().value())
                .name(emsp.name())
                .build();
    }

    public static Emsp toDomain(EmspEntity entity) {
        return new Emsp(
                new EmspId(entity.getId()),
                entity.getName()
        );
    }

    public static EmspResponse toResponse(Emsp emsp) {
        return new EmspResponse(
                emsp.id().value(),
                emsp.name()
        );
    }
}
