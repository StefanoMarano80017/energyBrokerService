package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.SupplierResponse;
import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import com.sch.energyBrokerService.domain.model.supplier.Supplier;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;

public class SupplierPersistenceMapper {

    public static SupplierEntity toEntity(Supplier supplier) {
        return SupplierEntity
                .builder()
                .id(supplier.id().value())
                .name(supplier.name())
                .build();
    }

    public static Supplier toDomain(SupplierEntity entity) {
        return new Supplier(
                new SupplierId(entity.getId()),
                entity.getName()
        );
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.id().value(),
                supplier.name()
        );
    }

    public static SupplierResponse toResponse(SupplierEntity entity) {
        return new SupplierResponse(
                entity.getId(),
                entity.getName()
        );
    }

}