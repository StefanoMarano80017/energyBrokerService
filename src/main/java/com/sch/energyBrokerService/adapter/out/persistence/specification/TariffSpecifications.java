package com.sch.energyBrokerService.adapter.out.persistence.specification;

import  com.sch.energyBrokerService.adapter.out.persistence.entity.TariffEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/*
* Le Specification sono criteri dinamici che puoi combinare senza creare mille metodi JPA.
*/
public class TariffSpecifications {

    public static Specification<TariffEntity> bySupplier(UUID supplierId) {
        return (root, query, cb) ->
                cb.equal(root.get("supplier").get("id"), supplierId);
    }

    public static Specification<TariffEntity> byGeoArea(Double latitude, Double longitude, Double radiusKm) {
        return (root, query, cb) -> {
            Double latDiff = radiusKm / 111.0; // Approx conversion km to degrees latitude
            Double lonDiff = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude))); // Adjust for longitude

            return cb.and(
                    cb.between(
                            root.get("coordinate").get("latitude"),
                            latitude - latDiff,
                            latitude + latDiff
                    ),
                    cb.between(
                            root.get("coordinate").get("longitude"),
                            longitude - lonDiff,
                            longitude + lonDiff
                    )
            );
        };
    }

    public static Specification<TariffEntity> byType(String type){
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<TariffEntity> byPrice(Double price){
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), price);
    }

}
