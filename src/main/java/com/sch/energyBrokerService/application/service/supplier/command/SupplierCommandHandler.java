package com.sch.energyBrokerService.application.service.supplier.command;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateSupplierRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.SupplierResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.SupplierPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaSupplierRepository;
import com.sch.energyBrokerService.domain.model.supplier.Supplier;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierCommandHandler {

    private final JpaSupplierRepository repository;

    public SupplierResponse create(CreateSupplierRequest request) {

        Supplier supplier = new Supplier(
                SupplierId.newId(),
                request.name()
        );

        repository.save(
                SupplierPersistenceMapper.toEntity(supplier)
        );

        return SupplierPersistenceMapper.toResponse(supplier);
    }
}