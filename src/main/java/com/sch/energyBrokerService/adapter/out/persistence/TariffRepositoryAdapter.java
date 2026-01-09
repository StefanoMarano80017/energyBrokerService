package com.sch.energyBrokerService.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.SupplierEntity;
import com.sch.energyBrokerService.adapter.out.persistence.entity.TariffEntity;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.TariffPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaTariffSpecificationRepository;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.repository.TariffRepository;

@Repository
public class TariffRepositoryAdapter implements TariffRepository {

    private final JpaTariffSpecificationRepository jpaRepo;

    public TariffRepositoryAdapter(JpaTariffSpecificationRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Tariff save(Tariff tariff) {
        TariffEntity entity = TariffPersistenceMapper.toEntity(tariff);
        TariffEntity saved = jpaRepo.save(entity);
        return TariffPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Tariff> findById(TariffId id) {
        return jpaRepo.findById(id.value()).map(TariffPersistenceMapper::toDomain);
    }

    @Override
    public List<Tariff> findAll() {
        return jpaRepo.findAll().stream().map(TariffPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Tariff> findBySupplierId(SupplierId supplierId) {
        return jpaRepo
                .findBySupplier(
                    SupplierEntity.builder().id(supplierId.value()).build()
                )
                .stream()
                .map(TariffPersistenceMapper::toDomain)
                .toList();
    }
}
