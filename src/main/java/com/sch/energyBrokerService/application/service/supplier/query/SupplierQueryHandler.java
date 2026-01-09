package com.sch.energyBrokerService.application.service.supplier.query;

import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.SupplierPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaSupplierRepository;
import com.sch.energyBrokerService.adapter.out.persistence.specification.SupplierSpecifications;
import com.sch.energyBrokerService.domain.model.supplier.Supplier;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierQueryHandler {

    private final JpaSupplierRepository repository;

    public Page<Supplier> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).map(SupplierPersistenceMapper::toDomain);
    }

    public Page<Supplier> searchSuppliers(String name, Pageable pageable) {

        Specification<SupplierEntity> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (name != null && !name.isBlank()) {
            spec = spec.and(SupplierSpecifications.byName(name));
        }

        Page<SupplierEntity> resultPage = repository.findAll(spec, pageable);
        return resultPage.map(SupplierPersistenceMapper::toDomain);
    }
}