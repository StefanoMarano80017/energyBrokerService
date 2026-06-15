package com.sch.energyBrokerService.adapter.out.persistence.repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JpaAgreementRepository
        extends JpaRepository<AgreementEntity, UUID>, JpaSpecificationExecutor<AgreementEntity> {
}
