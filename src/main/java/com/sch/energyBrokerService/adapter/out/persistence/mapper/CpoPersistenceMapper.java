package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.CpoResponse;
import com.sch.energyBrokerService.adapter.out.persistence.entity.CoordinateEmbeddable;
import com.sch.energyBrokerService.adapter.out.persistence.entity.CpoEntity;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

public class CpoPersistenceMapper {

    public static CpoEntity toEntity(Cpo cpo) {
        return CpoEntity.builder()
                .id(cpo.id().value())
                .name(cpo.name())
                .location(new CoordinateEmbeddable(
                        cpo.location().latitude(),
                        cpo.location().longitude()
                ))
                .build();
    }

    public static Cpo toDomain(CpoEntity entity) {
        return new Cpo(
                new CpoId(entity.getId()),
                entity.getName(),
                new Coordinate(
                        entity.getLocation().getLatitude(),
                        entity.getLocation().getLongitude()
                )
        );
    }

    public static CpoResponse toResponse(Cpo cpo) {
        return new CpoResponse(
                cpo.id().value(),
                cpo.name(),
                cpo.location().latitude(),
                cpo.location().longitude()
        );
    }
}
