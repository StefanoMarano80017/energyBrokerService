package com.sch.energyBrokerService.application.service.agreement.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgreementQueryHandler {

    private final AgreementRepository agreementRepository;

    public List<Agreement> search(UUID cpoId, UUID emspId, AgreementStatus status) {
        return agreementRepository.search(
                cpoId != null ? new CpoId(cpoId) : null,
                emspId != null ? new EmspId(emspId) : null,
                status
        );
    }

    public Optional<Agreement> findById(UUID id) {
        return agreementRepository.findById(new AgreementId(id));
    }
}
