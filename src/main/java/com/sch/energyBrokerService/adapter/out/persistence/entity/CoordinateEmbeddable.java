package com.sch.energyBrokerService.adapter.out.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CoordinateEmbeddable {
    private double latitude;
    private double longitude;
}
