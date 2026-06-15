package com.sch.energyBrokerService.domain.model.agreement.repository;

import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

import java.util.List;
import java.util.Optional;

public interface AgreementRepository {
    Agreement save(Agreement agreement);
    Optional<Agreement> findById(AgreementId id);

    /**
     * Ricerca filtrata: ogni filtro è opzionale (null = non applicato).
     * Implementata nell'adapter tramite Specification.
     */
    List<Agreement> search(CpoId cpoId, EmspId emspId, AgreementStatus status);
}
