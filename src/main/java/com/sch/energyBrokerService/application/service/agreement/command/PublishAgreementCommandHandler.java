package com.sch.energyBrokerService.application.service.agreement.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;

import lombok.RequiredArgsConstructor;

/**
 * Pubblicazione di un accordo: un CPO propone una tariffa a un eMSP.
 * Verifica l'esistenza di CPO ed eMSP, costruisce la tariffa e crea l'accordo in stato PROPOSED.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PublishAgreementCommandHandler {

    private final AgreementRepository agreementRepository;
    private final CpoRepository cpoRepository;
    private final EmspRepository emspRepository;

    public Agreement handle(CreateAgreementRequest request) {
        CpoId cpoId = new CpoId(request.cpoId());
        EmspId emspId = new EmspId(request.emspId());

        cpoRepository.findById(cpoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cpo not found: " + request.cpoId()));
        emspRepository.findById(emspId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Emsp not found: " + request.emspId()));

        Tariff tariff = new Tariff(
                TariffId.newId(),
                request.tariff().type(),
                new PricePerKwh(request.tariff().pricePerKwh())
        );

        Agreement agreement = Agreement.propose(cpoId, emspId, tariff);
        return agreementRepository.save(agreement);
    }
}
