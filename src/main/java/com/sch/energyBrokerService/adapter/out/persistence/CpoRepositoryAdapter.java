package com.sch.energyBrokerService.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaCpoRepository;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;

@Repository
public class CpoRepositoryAdapter implements CpoRepository {

    private final JpaCpoRepository jpaRepo;

    public CpoRepositoryAdapter(JpaCpoRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Cpo save(Cpo cpo) {
        var saved = jpaRepo.save(CpoPersistenceMapper.toEntity(cpo));
        return CpoPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Cpo> findById(CpoId id) {
        return jpaRepo.findById(id.value()).map(CpoPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Cpo> findByExternalId(String externalId) {
        return jpaRepo.findByExternalId(externalId).map(CpoPersistenceMapper::toDomain);
    }

    @Override
    public List<Cpo> findAll() {
        return jpaRepo.findAll().stream().map(CpoPersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteById(CpoId id) {
        jpaRepo.deleteById(id.value());
    }
}
