package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffResponse;
import com.sch.energyBrokerService.adapter.out.persistence.entity.*;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.geo.GeoArea;
import com.sch.energyBrokerService.domain.model.tariff.*;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;

/*
*   Adapter che implementa il repository del dominio, e facciamo il mapping tra JPA Entities e Domain Model.
*/
public class TariffPersistenceMapper {

    // Domain -> Entity
    public static TariffEntity toEntity(Tariff domain) {
        return TariffEntity.builder()
                .id(domain.id().value())
                .supplier(SupplierEntity.builder().id(domain.supplierId().value()).build())
                .type(TariffType.valueOf(domain.type().name()))
                .pricePerKwh(new PricePerKwhEmbeddable(domain.pricePerKwh().amount()))
                .coordinate(new CoordinateEmbeddable(
                        domain.geoArea().center().latitude(),
                        domain.geoArea().center().longitude())
                )
                .geoRadiusKm(domain.geoArea().radiusKm())
                .build();
    }

    // Entity -> Domain
    public static Tariff toDomain(TariffEntity entity) {
        return new Tariff(
                new TariffId(entity.getId()),
                new SupplierId(entity.getSupplier().getId()),
                TariffType.valueOf(entity.getType().name()),
                new PricePerKwh(entity.getPricePerKwh().getAmount()),
                new GeoArea(
                        new Coordinate(
                                entity.getCoordinate().getLatitude(),
                                entity.getCoordinate().getLongitude()
                        ),
                        entity.getGeoRadiusKm()
                )
        );
    }

    public static TariffResponse toResponse(Tariff domain) {
        return new TariffResponse(
                domain.id().value(),
                null,
                domain.supplierId().value().toString(),
                domain.type().toString(),
                domain.pricePerKwh().amount(),
                domain.geoArea().center().latitude(),
                domain.geoArea().center().longitude(),
                domain.geoArea().radiusKm()
        );
    }

    public static TariffResponse toResponse(TariffEntity entity) {
        return new TariffResponse(
                entity.getId(),
                entity.getSupplier().getName(),
                null,
                entity.getType().toString(),
                entity.getPricePerKwh().getAmount(),
                entity.getCoordinate().getLatitude(),
                entity.getCoordinate().getLongitude(),
                entity.getGeoRadiusKm()
        );
    }
}
