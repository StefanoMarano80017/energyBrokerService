package com.sch.energyBrokerService.application.service.pricing.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;
import com.sch.energyBrokerService.domain.model.agreement.repository.AgreementRepository;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;
import com.sch.energyBrokerService.domain.model.tariff.pricing.FlatPricingStrategy;
import com.sch.energyBrokerService.domain.model.tariff.pricing.TariffPriceCalculator;

class PriceQuoteQueryHandlerTest {

    private final CpoRepository cpoRepository = mock(CpoRepository.class);
    private final AgreementRepository agreementRepository = mock(AgreementRepository.class);
    private final TariffPriceCalculator calculator =
            new TariffPriceCalculator(List.of(new FlatPricingStrategy()));
    private final PriceQuoteQueryHandler handler =
            new PriceQuoteQueryHandler(cpoRepository, agreementRepository, calculator);

    @Test
    void returnsTheCalculatedQuoteForAnAcceptedAgreement() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", new Coordinate(45, 9));
        Agreement agreement = Agreement.propose(
                cpo.id(),
                new EmspId(UUID.randomUUID()),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42"))));
        agreement.accept();
        when(cpoRepository.findByNameIgnoreCase("Charge Spa")).thenReturn(List.of(cpo));
        when(agreementRepository.search(cpo.id(), null, AgreementStatus.ACCEPTED))
                .thenReturn(List.of(agreement));

        var quotes = handler.quote("Charge Spa", new BigDecimal("10"));

        assertThat(quotes).hasSize(1);
        assertThat(quotes.get(0).agreementId()).isEqualTo(agreement.id().value());
        assertThat(quotes.get(0).type()).isEqualTo("FLAT");
        assertThat(quotes.get(0).pricePerKwh()).isEqualByComparingTo("0.4200");
        assertThat(quotes.get(0).kwh()).isEqualByComparingTo("10");
        assertThat(quotes.get(0).totalCost()).isEqualByComparingTo("4.20");
    }

    @Test
    void reportsWhenNoCpoMatchesTheRequestedName() {
        when(cpoRepository.findByNameIgnoreCase("Unknown")).thenReturn(List.of());

        assertThatThrownBy(() -> handler.quote("Unknown", BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cpo not found: Unknown");
    }

    @Test
    void returnsAnEmptyListWhenTheCpoHasNoAcceptedAgreements() {
        Cpo cpo = new Cpo(CpoId.newId(), null, "Charge Spa", new Coordinate(45, 9));
        when(cpoRepository.findByNameIgnoreCase("Charge Spa")).thenReturn(List.of(cpo));
        when(agreementRepository.search(cpo.id(), null, AgreementStatus.ACCEPTED)).thenReturn(List.of());

        assertThat(handler.quote("Charge Spa", BigDecimal.TEN)).isEmpty();
    }

    @Test
    void includesQuotesFromEveryCpoMatchingTheSameName() {
        Cpo firstCpo = new Cpo(CpoId.newId(), null, "Charge Spa", new Coordinate(45, 9));
        Cpo secondCpo = new Cpo(CpoId.newId(), null, "Charge Spa", new Coordinate(46, 10));
        Agreement firstAgreement = acceptedAgreement(firstCpo, "0.20");
        Agreement secondAgreement = acceptedAgreement(secondCpo, "0.30");
        when(cpoRepository.findByNameIgnoreCase("Charge Spa")).thenReturn(List.of(firstCpo, secondCpo));
        when(agreementRepository.search(firstCpo.id(), null, AgreementStatus.ACCEPTED))
                .thenReturn(List.of(firstAgreement));
        when(agreementRepository.search(secondCpo.id(), null, AgreementStatus.ACCEPTED))
                .thenReturn(List.of(secondAgreement));

        var quotes = handler.quote("Charge Spa", BigDecimal.TEN);

        assertThat(quotes).hasSize(2);
        assertThat(quotes).extracting(quote -> quote.cpoId())
                .containsExactly(firstCpo.id().value(), secondCpo.id().value());
        assertThat(quotes).extracting(quote -> quote.totalCost())
                .containsExactly(new BigDecimal("2.00"), new BigDecimal("3.00"));
    }

    private static Agreement acceptedAgreement(Cpo cpo, String price) {
        Agreement agreement = Agreement.propose(
                cpo.id(),
                new EmspId(UUID.randomUUID()),
                new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal(price))));
        agreement.accept();
        return agreement;
    }
}
