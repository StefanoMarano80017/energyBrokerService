package com.sch.energyBrokerService.domain.model.emsp.repository;

import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;

import java.util.List;
import java.util.Optional;

public interface EmspRepository {
    Emsp save(Emsp emsp);
    Optional<Emsp> findById(EmspId id);
    List<Emsp> findAll();
    void deleteById(EmspId id);
}
