package com.sch.energyBrokerService.adapter.out.persistence.specification;

import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import org.springframework.data.jpa.domain.Specification;

public class SupplierSpecifications {

    public static Specification<SupplierEntity> byName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), 
                        "%" + name.toLowerCase() + "%"
                );
    }
}