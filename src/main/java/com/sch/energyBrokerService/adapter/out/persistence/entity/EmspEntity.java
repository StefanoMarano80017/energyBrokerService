package com.sch.energyBrokerService.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "emsps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmspEntity {

    @Id
    private UUID id;
    private String name;
}
