package com.sch.energyBrokerService.adapter.out.persistence.repository;


import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, UUID>, JpaSpecificationExecutor<SupplierEntity> { 

        @Query("SELECT s FROM SupplierEntity s")
        List<SupplierEntity> findAllSuppliers();

}