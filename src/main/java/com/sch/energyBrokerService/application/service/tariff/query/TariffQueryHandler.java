package com.sch.energyBrokerService.application.service.tariff.query;

import com.sch.energyBrokerService.adapter.out.persistence.entity.TariffEntity;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaTariffSpecificationRepository;
import com.sch.energyBrokerService.adapter.out.persistence.specification.TariffSpecifications;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(TariffQueryHandler.class);

    private final JpaTariffSpecificationRepository repository;

    public Page<TariffEntity> searchTariffs(
            UUID supplierId,
            Double latitude,
            Double longitude,
            Double radiusKm,
            Double price,
            TariffType type,
            Pageable pageable
    ) {
        
        log.info("Search tariffs called: supplierId={}, lat={}, lon={}, radius={}, page={}", 
                supplierId, latitude, longitude, radiusKm, pageable);

        Specification<TariffEntity> spec = (root, query, cb) -> {
                root.join("supplier", JoinType.LEFT); 
                return cb.conjunction();
        };

        if (supplierId != null) {
            spec = spec.and(TariffSpecifications.bySupplier(supplierId));
        }

        if(latitude != null && longitude != null && radiusKm != null) {
            spec = spec.and(TariffSpecifications.byGeoArea(latitude, longitude, radiusKm));
        }

        if(type != null) {
            spec = spec.and(TariffSpecifications.byType(type.name()));
        }

        if(price != null) {
            spec = spec.and(TariffSpecifications.byPrice(price));
        }

        return repository.findAll(spec, pageable);
    }

    public Page<TariffEntity> findCoveringTariffs(
            UUID supplierId,
            Double latitude,
            Double longitude,
            Pageable pageable
    ) {

        if (latitude == null || longitude == null) {
            log.info("Msissing coordinates, returning empty page");
            return Page.empty(pageable);
        }

        log.info("Search tariffs called: supplierId={}, lat={}, lon={}, page={}", supplierId, latitude, longitude, pageable);

        Specification<TariffEntity> spec = (root, query, cb) -> {
                root.join("supplier", JoinType.LEFT); 
                return cb.conjunction();
        };

        if (supplierId != null) {
            spec = spec.and(TariffSpecifications.bySupplier(supplierId));
        }

        Page<TariffEntity> page = repository.findAll(spec, pageable);

        List<TariffEntity> filtered = page.getContent().stream()
                .filter(t -> 
                        latitude == null  || 
                        longitude == null || 
                        distanceKm(
                                latitude,
                                longitude,
                                t.getCoordinate().getLatitude(),
                                t.getCoordinate().getLongitude()
                        ) <= t.getGeoRadiusKm()
                ).toList();

        // Page manuale
        return toPage(filtered, pageable);
    }

    // Calcolo distanza Haversine
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // raggio terrestre km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        int total = list.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        List<T> content = start <= end ? list.subList(start, end) : List.of();

        return new PageImpl<>(content, pageable, total);
    }

}