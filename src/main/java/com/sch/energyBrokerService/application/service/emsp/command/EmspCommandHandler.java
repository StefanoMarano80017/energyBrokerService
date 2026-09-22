package com.sch.energyBrokerService.application.service.emsp.command;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.emsp.Emsp;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.emsp.repository.EmspRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmspCommandHandler {

    private final EmspRepository emspRepository;

    public Emsp create(CreateEmspRequest request) {
        EmspId emspId = EmspId.newId();
        requireExternalIdAvailable(request.externalId(), emspId);

        Emsp emsp = new Emsp(emspId, request.externalId(), request.name());
        return emspRepository.save(emsp);
    }

    public Emsp update(UUID id, CreateEmspRequest request) {
        EmspId emspId = new EmspId(id);
        emspRepository.findById(emspId)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));
        requireExternalIdAvailable(request.externalId(), emspId);

        Emsp updated = new Emsp(emspId, request.externalId(), request.name());
        return emspRepository.save(updated);
    }

    public void delete(UUID id) {
        EmspId emspId = new EmspId(id);
        emspRepository.findById(emspId)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));
        emspRepository.deleteById(emspId);
    }

    /**
     * L'externalId è opzionale ma univoco: verifica che non sia già assegnato a un altro eMSP.
     * {@code owner} è l'entità che lo sta rivendicando, esclusa dal controllo (caso update).
     */
    private void requireExternalIdAvailable(String externalId, EmspId owner) {
        if (externalId == null || externalId.isBlank()) {
            return;
        }
        emspRepository.findByExternalId(externalId.trim()).ifPresent(existing -> {
            if (!existing.id().equals(owner)) {
                throw new IllegalArgumentException(
                        "Emsp externalId already in use: " + externalId.trim());
            }
        });
    }
}
