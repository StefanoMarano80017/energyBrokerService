package com.sch.energyBrokerService.adapter.out.persistence.repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.CpoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCpoRepository extends JpaRepository<CpoEntity, UUID> {
    Optional<CpoEntity> findByExternalId(String externalId);
    List<CpoEntity> findByNameIgnoreCase(String name);
}
