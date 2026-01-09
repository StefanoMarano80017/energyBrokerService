package com.sch.energyBrokerService.domain.model.supplier.repository;

import com.sch.energyBrokerService.domain.model.supplier.Supplier;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(SupplierId id);
    List<Supplier> findAll();
}