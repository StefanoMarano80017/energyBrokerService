package com.sch.energyBrokerService.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.sch.energyBrokerService.adapter.out.persistence.entity.AgreementEntity;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.repository.JpaAgreementRepository;
import com.sch.energyBrokerService.adapter.out.persistence.specification.AgreementSpecifications;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

@Repository
public class AgreementRepositoryAdapter implements AgreementRepository {

    private final JpaAgreementRepository jpaRepo;

    public AgreementRepositoryAdapter(JpaAgreementRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Agreement save(Agreement agreement) {
        var saved = jpaRepo.save(AgreementPersistenceMapper.toEntity(agreement));
        return AgreementPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Agreement> findById(AgreementId id) {
        return jpaRepo.findById(id.value()).map(AgreementPersistenceMapper::toDomain);
    }

    @Override
    public List<Agreement> search(CpoId cpoId, EmspId emspId, AgreementStatus status) {
        List<Specification<AgreementEntity>> filters = new ArrayList<>();

        if (cpoId != null) {
            filters.add(AgreementSpecifications.byCpo(cpoId.value()));
        }
        if (emspId != null) {
            filters.add(AgreementSpecifications.byEmsp(emspId.value()));
        }
        if (status != null) {
            filters.add(AgreementSpecifications.byStatus(status));
        }

        return jpaRepo.findAll(Specification.allOf(filters)).stream()
                .map(AgreementPersistenceMapper::toDomain)
                .toList();
    }
}
