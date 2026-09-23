package com.sch.energyBrokerService.domain.model.cpo.repository;

import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;

import java.util.List;
import java.util.Optional;

public interface CpoRepository {
    Cpo save(Cpo cpo);
    Optional<Cpo> findById(CpoId id);

    /** Lookup per identificativo assegnato dal sistema esterno (univoco quando valorizzato). */
    Optional<Cpo> findByExternalId(String externalId);

    /** Lookup per nome senza distinzione fra maiuscole e minuscole (il nome non è univoco). */
    List<Cpo> findByNameIgnoreCase(String name);

    List<Cpo> findAll();
    void deleteById(CpoId id);
}
