package com.sch.energyBrokerService.domain.model.tariff.pricing;

import java.math.BigDecimal;

import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/**
 * Tariffa DYNAMIC: prezzo legato all'andamento del mercato.
 *
 * <p>TODO: non esiste ancora una sorgente di prezzi di mercato, quindi per ora si applica
 * {@code pricePerKwh} come prezzo unico. Qui andrà la logica dinamica.
 */
public class DynamicPricingStrategy implements TariffPricingStrategy {

    @Override
    public TariffType supportedType() {
        return TariffType.DYNAMIC;
    }

    @Override
    public BigDecimal calculate(Tariff tariff, BigDecimal kwh) {
        return tariff.pricePerKwh().multiply(kwh);
    }
}
