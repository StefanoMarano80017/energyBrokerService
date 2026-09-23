package com.sch.energyBrokerService.domain.model.tariff.pricing;

import java.math.BigDecimal;

import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/** Tariffa FLAT: prezzo unico per kWh. */
public class FlatPricingStrategy implements TariffPricingStrategy {

    @Override
    public TariffType supportedType() {
        return TariffType.FLAT;
    }

    @Override
    public BigDecimal calculate(Tariff tariff, BigDecimal kwh) {
        return tariff.pricePerKwh().multiply(kwh);
    }
}
