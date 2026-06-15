package com.sch.energyBrokerService.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaEmspRepository;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;

@Repository
public class EmspRepositoryAdapter implements EmspRepository {

    private final JpaEmspRepository jpaRepo;

    public EmspRepositoryAdapter(JpaEmspRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Emsp save(Emsp emsp) {
        var saved = jpaRepo.save(EmspPersistenceMapper.toEntity(emsp));
        return EmspPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Emsp> findById(EmspId id) {
        return jpaRepo.findById(id.value()).map(EmspPersistenceMapper::toDomain);
    }

    @Override
    public List<Emsp> findAll() {
        return jpaRepo.findAll().stream().map(EmspPersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteById(EmspId id) {
        jpaRepo.deleteById(id.value());
    }
}
