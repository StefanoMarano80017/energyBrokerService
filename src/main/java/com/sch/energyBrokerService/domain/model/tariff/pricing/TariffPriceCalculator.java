package com.sch.energyBrokerService.domain.model.tariff.pricing;

import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Seleziona la {@link TariffPricingStrategy} in base al {@link TariffType} della tariffa
 * e ne delega il calcolo. Ogni tipo può avere al più una strategia.
 */
public class TariffPriceCalculator {

    private final Map<TariffType, TariffPricingStrategy> strategies = new EnumMap<>(TariffType.class);

    public TariffPriceCalculator(List<TariffPricingStrategy> strategies) {
        for (TariffPricingStrategy strategy : strategies) {
            TariffPricingStrategy previous = this.strategies.put(strategy.supportedType(), strategy);
            if (previous != null) {
                throw new IllegalStateException(
                    "Multiple pricing strategies for tariff type " +
                        strategy.supportedType() +
                        ": " +
                        previous.getClass().getSimpleName() +
                        ", " +
                        strategy.getClass().getSimpleName()
                );
            }
        }
    }

    public BigDecimal calculate(Tariff tariff, BigDecimal kwh) {
        if (kwh == null || kwh.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("kWh must be positive");
        }
        TariffPricingStrategy strategy = strategies.get(tariff.type());
        if (strategy == null) {
            throw new IllegalStateException("No pricing strategy for tariff type " + tariff.type());
        }
        return strategy.calculate(tariff, kwh);
    }
}
