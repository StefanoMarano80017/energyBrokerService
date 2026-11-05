package com.sch.energyBrokerService.domain.model.tariff.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TariffPriceCalculatorTest {

    private final TariffPriceCalculator calculator = new TariffPriceCalculator(
        List.of(new FlatPricingStrategy(), new TimeBasedPricingStrategy(), new DynamicPricingStrategy())
    );

    @Test
    void selectsTheMatchingStrategyForEveryTariffType() {
        Tariff flat = new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42")));

        assertThat(calculator.calculate(flat, new BigDecimal("10"))).isEqualByComparingTo(new BigDecimal("4.20"));

        Tariff timeBased = new Tariff(TariffId.newId(), TariffType.TIME_BASED, new PricePerKwh(new BigDecimal("0.31")));
        assertThat(calculator.calculate(timeBased, new BigDecimal("25.5"))).isEqualByComparingTo(
            new BigDecimal("7.91")
        );

        Tariff dynamic = new Tariff(TariffId.newId(), TariffType.DYNAMIC, new PricePerKwh(new BigDecimal("0.45")));
        assertThat(calculator.calculate(dynamic, new BigDecimal("15.75"))).isEqualByComparingTo(new BigDecimal("7.09"));
    }

    @ParameterizedTest
    @MethodSource("invalidKwhValues")
    void rejectsInvalidConsumedEnergy(BigDecimal kwh) {
        Tariff flat = new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42")));

        assertThatThrownBy(() -> calculator.calculate(flat, kwh))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("kWh must be positive");
    }

    private static Stream<BigDecimal> invalidKwhValues() {
        return Stream.of(null, BigDecimal.ZERO, new BigDecimal("-1.0"), new BigDecimal("-0.01"));
    }

    @Test
    void rejectsDuplicatePricingStrategiesForTheSameTariffType() {
        assertThatThrownBy(() ->
            new TariffPriceCalculator(List.of(new FlatPricingStrategy(), new FlatPricingStrategy()))
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Multiple pricing strategies");
    }

    @Test
    void rejectsNullConsumedEnergy() {
        Tariff flat = new Tariff(TariffId.newId(), TariffType.FLAT, new PricePerKwh(new BigDecimal("0.42")));

        assertThatThrownBy(() -> calculator.calculate(flat, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("kWh must be positive");
    }

    @ParameterizedTest
    @EnumSource(TariffType.class)
    void rejectsEveryTariffTypeWithoutRegisteredStrategy(TariffType type) {
        TariffPriceCalculator emptyCalculator = new TariffPriceCalculator(List.of());

        Tariff tariff = new Tariff(TariffId.newId(), type, new PricePerKwh(new BigDecimal("0.42")));

        assertThatThrownBy(() -> emptyCalculator.calculate(tariff, new BigDecimal("10")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No pricing strategy");
    }
}
