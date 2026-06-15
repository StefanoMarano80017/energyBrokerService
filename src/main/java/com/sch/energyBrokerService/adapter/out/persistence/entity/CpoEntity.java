package com.sch.energyBrokerService.adapter.out.persistence.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cpos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CpoEntity {

    @Id
    private UUID id;
    private String name;

    @Embedded
    private CoordinateEmbeddable location;
}
