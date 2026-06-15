package com.sch.energyBrokerService.adapter.out.persistence.specification;

import com.sch.energyBrokerService.adapter.out.persistence.entity.AgreementEntity;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Criteri dinamici combinabili per la ricerca degli accordi.
 */
public class AgreementSpecifications {

    public static Specification<AgreementEntity> byCpo(UUID cpoId) {
        return (root, query, cb) -> cb.equal(root.get("cpoId"), cpoId);
    }

    public static Specification<AgreementEntity> byEmsp(UUID emspId) {
        return (root, query, cb) -> cb.equal(root.get("emspId"), emspId);
    }

    public static Specification<AgreementEntity> byStatus(AgreementStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
