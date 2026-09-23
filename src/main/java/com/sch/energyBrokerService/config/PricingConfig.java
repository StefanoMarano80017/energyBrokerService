package com.sch.energyBrokerService.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sch.energyBrokerService.domain.model.tariff.pricing.DynamicPricingStrategy;
import com.sch.energyBrokerService.domain.model.tariff.pricing.FlatPricingStrategy;
import com.sch.energyBrokerService.domain.model.tariff.pricing.TariffPriceCalculator;
import com.sch.energyBrokerService.domain.model.tariff.pricing.TariffPricingStrategy;
import com.sch.energyBrokerService.domain.model.tariff.pricing.TimeBasedPricingStrategy;

/**
 * Registrazione delle strategie di calcolo prezzo. Per un nuovo tipo di tariffa
 * è sufficiente aggiungere qui il bean della relativa {@link TariffPricingStrategy}.
 */
@Configuration
public class PricingConfig {

    @Bean
    public TariffPricingStrategy flatPricingStrategy() {
        return new FlatPricingStrategy();
    }

    @Bean
    public TariffPricingStrategy timeBasedPricingStrategy() {
        return new TimeBasedPricingStrategy();
    }

    @Bean
    public TariffPricingStrategy dynamicPricingStrategy() {
        return new DynamicPricingStrategy();
    }

    @Bean
    public TariffPriceCalculator tariffPriceCalculator(List<TariffPricingStrategy> strategies) {
        return new TariffPriceCalculator(strategies);
    }
}
