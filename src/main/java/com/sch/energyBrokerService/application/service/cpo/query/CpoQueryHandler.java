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

    public List<Cpo> findAll() {
        return cpoRepository.findAll();
    }

    public Optional<Cpo> findById(UUID id) {
        return cpoRepository.findById(new CpoId(id));
    }
}
