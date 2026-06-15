package com.sch.energyBrokerService.adapter.out.persistence.entity;

import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Tariff persistita come parte dell'aggregato Agreement (nessuna tabella separata).
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TariffEmbeddable {

    @Column(name = "tariff_id")
    private UUID tariffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type")
    private TariffType type;

    @Embedded
    private PricePerKwhEmbeddable pricePerKwh;
}
