package com.sch.energyBrokerService.adapter.out.persistence.repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import com.sch.energyBrokerService.adapter.out.persistence.entity.TariffEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaTariffSpecificationRepository extends JpaRepository<TariffEntity, UUID>, JpaSpecificationExecutor<TariffEntity> {
    List<TariffEntity> findBySupplier(SupplierEntity supplier);
    
    @Query("SELECT t FROM TariffEntity t JOIN FETCH t.supplier")
    List<TariffEntity> findAllWithSupplier();
}
