package com.sch.energyBrokerService.adapter.out.persistence.repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.CpoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCpoRepository extends JpaRepository<CpoEntity, UUID> {
}
