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

    public List<Emsp> search(String name) {
        List<Emsp> all = emspRepository.findAll();
        if (name == null || name.isBlank()) {
            return all;
        }
        String needle = name.toLowerCase();
        return all.stream()
                .filter(e -> e.name().toLowerCase().contains(needle))
                .toList();
    }

    public Optional<Emsp> findById(UUID id) {
        return emspRepository.findById(new EmspId(id));
    }
}
