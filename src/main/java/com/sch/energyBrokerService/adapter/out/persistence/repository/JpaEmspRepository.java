package com.sch.energyBrokerService.adapter.out.persistence.repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.EmspEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaEmspRepository extends JpaRepository<EmspEntity, UUID> {
    Optional<EmspEntity> findByExternalId(String externalId);
}
