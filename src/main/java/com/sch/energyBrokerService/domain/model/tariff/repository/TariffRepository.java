package com.sch.energyBrokerService.domain.model.tariff.repository;

import java.util.List;
import java.util.Optional;


import com.sch.energyBrokerService.domain.model.supplier.SupplierId;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;

public interface TariffRepository {
    Tariff save(Tariff tariff);
    Optional<Tariff> findById(TariffId id);
    List<Tariff> findAll();
    List<Tariff> findBySupplierId(SupplierId supplierId);
}
