package com.sch.energyBrokerService.application.service.cpo.command;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.domain.model.cpo.Cpo;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.cpo.repository.CpoRepository;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CpoCommandHandler {

    private final CpoRepository cpoRepository;

    public Cpo create(CreateCpoRequest request) {
        CpoId cpoId = CpoId.newId();
        requireExternalIdAvailable(request.externalId(), cpoId);

        Cpo cpo = new Cpo(
                cpoId,
                request.externalId(),
                request.name(),
                new Coordinate(request.latitude(), request.longitude())
        );
        return cpoRepository.save(cpo);
    }

    public Cpo update(UUID id, CreateCpoRequest request) {
        CpoId cpoId = new CpoId(id);
        cpoRepository.findById(cpoId)
                .orElseThrow(() -> new EntityNotFoundException("Cpo not found: " + id));
        requireExternalIdAvailable(request.externalId(), cpoId);

        Cpo updated = new Cpo(
                cpoId,
                request.externalId(),
                request.name(),
                new Coordinate(request.latitude(), request.longitude())
        );
        return cpoRepository.save(updated);
    }

    public void delete(UUID id) {
        CpoId cpoId = new CpoId(id);
        cpoRepository.findById(cpoId)
                .orElseThrow(() -> new EntityNotFoundException("Cpo not found: " + id));
        cpoRepository.deleteById(cpoId);
    }

    /**
     * L'externalId è opzionale ma univoco: verifica che non sia già assegnato a un altro CPO.
     * {@code owner} è l'entità che lo sta rivendicando, esclusa dal controllo (caso update).
     */
    private void requireExternalIdAvailable(String externalId, CpoId owner) {
        if (externalId == null || externalId.isBlank()) {
            return;
        }
        cpoRepository.findByExternalId(externalId.trim()).ifPresent(existing -> {
            if (!existing.id().equals(owner)) {
                throw new IllegalArgumentException(
                        "Cpo externalId already in use: " + externalId.trim());
            }
        });
    }
}
