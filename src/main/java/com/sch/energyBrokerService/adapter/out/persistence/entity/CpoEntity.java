package com.sch.energyBrokerService.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "cpos",
        uniqueConstraints = @UniqueConstraint(name = "uk_cpos_external_id", columnNames = "external_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CpoEntity {

    @Id
    private UUID id;

    /** Identificativo del sistema esterno: opzionale, univoco quando valorizzato. */
    @Column(name = "external_id", unique = true)
    private String externalId;

    private String name;

    @Embedded
    private CoordinateEmbeddable location;
}
