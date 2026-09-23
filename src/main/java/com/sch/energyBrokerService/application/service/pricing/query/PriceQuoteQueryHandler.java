package com.sch.energyBrokerService.application.service.pricing.query;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffQuoteResponse;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.pricing.TariffPriceCalculator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceQuoteQueryHandler {

    private final CpoRepository cpoRepository;
    private final AgreementRepository agreementRepository;
    private final TariffPriceCalculator priceCalculator;

    /**
     * Calcola il costo dei {@code kwh} richiesti per ogni tariffa degli accordi ACCEPTED
     * dei CPO con il nome indicato. Il nome non è univoco: se più CPO corrispondono,
     * vengono considerati tutti.
     */
    public List<TariffQuoteResponse> quote(String cpoName, BigDecimal kwh) {
        List<Cpo> cpos = cpoRepository.findByNameIgnoreCase(cpoName.trim());
        if (cpos.isEmpty()) {
            throw new EntityNotFoundException("Cpo not found: " + cpoName);
        }
        return cpos.stream()
                .flatMap(cpo -> agreementRepository.search(cpo.id(), null, AgreementStatus.ACCEPTED).stream())
                .map(agreement -> toQuote(agreement, kwh))
                .toList();
    }

    private TariffQuoteResponse toQuote(Agreement agreement, BigDecimal kwh) {
        Tariff tariff = agreement.tariff();
        return new TariffQuoteResponse(
                agreement.id().value(),
                agreement.cpoId().value(),
                agreement.emspId().value(),
                tariff.id().value(),
                tariff.type().name(),
                tariff.pricePerKwh().amount(),
                kwh,
                priceCalculator.calculate(tariff, kwh)
        );
    }
}
