package com.sch.energyBrokerService.domain.model.tariff.pricing;

import java.math.BigDecimal;

import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/**
 * Tariffa TIME_BASED: prezzo variabile per fascia oraria.
 *
 * <p>TODO: la {@link Tariff} non modella ancora le fasce orarie, quindi per ora si applica
 * {@code pricePerKwh} come prezzo unico. Qui andrà la logica per fascia.
 */
public class TimeBasedPricingStrategy implements TariffPricingStrategy {

    @Override
    public TariffType supportedType() {
        return TariffType.TIME_BASED;
    }

    @Override
    public BigDecimal calculate(Tariff tariff, BigDecimal kwh) {
        return tariff.pricePerKwh().multiply(kwh);
    }
}
