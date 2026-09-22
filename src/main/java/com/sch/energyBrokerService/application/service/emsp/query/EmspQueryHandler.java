package com.sch.energyBrokerService.application.service.emsp.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmspQueryHandler {

    private final EmspRepository emspRepository;

    /**
     * Ricerca con filtri opzionali. Con {@code externalId} valorizzato la lista contiene
     * al più un elemento, perché l'identificativo esterno è univoco; il filtro sul nome
     * resta applicabile in combinazione.
     */
    public List<Emsp> search(String name, String externalId) {
        List<Emsp> candidates;
        if (externalId != null && !externalId.isBlank()) {
            candidates = emspRepository.findByExternalId(externalId.trim())
                    .map(List::of)
                    .orElseGet(List::of);
        } else {
            candidates = emspRepository.findAll();
        }

        if (name == null || name.isBlank()) {
            return candidates;
        }
        String needle = name.toLowerCase();
        return candidates.stream()
                .filter(e -> e.name().toLowerCase().contains(needle))
                .toList();
    }

    public Optional<Emsp> findById(UUID id) {
        return emspRepository.findById(new EmspId(id));
    }

    public Optional<Emsp> findByExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            return Optional.empty();
        }
        return emspRepository.findByExternalId(externalId.trim());
    }
}
