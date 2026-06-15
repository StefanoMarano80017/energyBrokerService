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
        Emsp emsp = new Emsp(EmspId.newId(), request.name());
        return emspRepository.save(emsp);
    }

    public Emsp update(UUID id, CreateEmspRequest request) {
        EmspId emspId = new EmspId(id);
        emspRepository.findById(emspId)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));

        Emsp updated = new Emsp(emspId, request.name());
        return emspRepository.save(updated);
    }

    public void delete(UUID id) {
        EmspId emspId = new EmspId(id);
        emspRepository.findById(emspId)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));
        emspRepository.deleteById(emspId);
    }
}
