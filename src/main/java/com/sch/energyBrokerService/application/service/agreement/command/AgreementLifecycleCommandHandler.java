package com.sch.energyBrokerService.application.service.agreement.command;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;

import lombok.RequiredArgsConstructor;

/**
 * Transizioni di stato sull'aggregato Agreement (accept / reject / cancel).
 * La regola di transizione vive nell'aggregato; qui si orchestrano load -> transizione -> save.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AgreementLifecycleCommandHandler {

    private final AgreementRepository agreementRepository;

    public Agreement accept(UUID id) {
        return apply(id, Agreement::accept);
    }

    public Agreement reject(UUID id) {
        return apply(id, Agreement::reject);
    }

    public Agreement cancel(UUID id) {
        return apply(id, Agreement::cancel);
    }

    private Agreement apply(UUID id, Consumer<Agreement> transition) {
        Agreement agreement = agreementRepository.findById(new AgreementId(id))
                .orElseThrow(() -> new EntityNotFoundException("Agreement not found: " + id));
        transition.accept(agreement);
        return agreementRepository.save(agreement);
    }
}
