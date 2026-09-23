package com.sch.energyBrokerService.adapter.out.persistence.entity;

import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agreements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementEntity {

    @Id
    private UUID id;

    @Column(name = "cpo_id")
    private UUID cpoId;

    @Column(name = "emsp_id")
    private UUID emspId;

    @Embedded
    private TariffEmbeddable tariff;

    @Enumerated(EnumType.STRING)
    private AgreementStatus status;

    private Instant createdAt;
    private Instant acceptedAt;
    private Instant lastModifiedAt;
}
