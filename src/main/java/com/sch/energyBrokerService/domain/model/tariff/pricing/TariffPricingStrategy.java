package com.sch.energyBrokerService.domain.model.tariff.pricing;

import java.math.BigDecimal;

import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/**
 * Strategia di calcolo del costo di una ricarica per uno specifico {@link TariffType}.
 *
 * <p>Per introdurre un nuovo tipo di tariffa: aggiungere il valore a {@link TariffType},
 * creare un'implementazione di questa interfaccia e registrarla come bean in
 * {@code PricingConfig}. {@link TariffPriceCalculator} la selezionerà automaticamente.
 */
public interface TariffPricingStrategy {

    /** Tipo di tariffa gestito da questa strategia. */
    TariffType supportedType();

    /** Costo totale per i {@code kwh} indicati, arrotondato a 2 decimali. */
    BigDecimal calculate(Tariff tariff, BigDecimal kwh);
}
