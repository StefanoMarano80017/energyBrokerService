package com.sch.energyBrokerService.application.service.cpo.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CpoQueryHandler {

    private final CpoRepository cpoRepository;

    /**
     * Ricerca con filtri opzionali. Con {@code externalId} valorizzato la lista contiene
     * al più un elemento, perché l'identificativo esterno è univoco.
     */
    public List<Cpo> search(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return cpoRepository.findAll();
        }
        return cpoRepository.findByExternalId(externalId.trim())
                .map(List::of)
                .orElseGet(List::of);
    }

    public List<Cpo> findAll() {
        return cpoRepository.findAll();
    }

    public Optional<Cpo> findById(UUID id) {
        return cpoRepository.findById(new CpoId(id));
    }

    public Optional<Cpo> findByExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return Optional.empty();
        }
        return cpoRepository.findByExternalId(externalId.trim());
    }
}
