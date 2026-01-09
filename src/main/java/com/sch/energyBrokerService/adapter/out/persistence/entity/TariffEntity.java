package com.sch.energyBrokerService.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import com.sch.energyBrokerService.domain.model.tariff.TariffType;

@Entity
@Table(name = "tariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @Enumerated(EnumType.STRING)
    private TariffType type;

    @Embedded
    private PricePerKwhEmbeddable pricePerKwh;

    @Embedded
    private CoordinateEmbeddable coordinate;

    private double geoRadiusKm; // raggio per il matching
}
