package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.CpoResponse;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.EmspResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ui/agreements")
@RequiredArgsConstructor
public class AgreementPageController {

    private final AgreementQueryHandler agreementQueryHandler;
    private final AgreementLifecycleCommandHandler lifecycleHandler;
    private final CpoQueryHandler cpoQueryHandler;
    private final EmspQueryHandler emspQueryHandler;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("agreements", agreementQueryHandler.search(null, null, null).stream()
                .map(AgreementPersistenceMapper::toResponse)
                .toList());
        addActorLookups(model);
        return "agreements";
    }

    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("agreements",
                agreementQueryHandler.search(null, null, AgreementStatus.PROPOSED).stream()
                        .map(AgreementPersistenceMapper::toResponse)
                        .toList());
        addActorLookups(model);
        return "agreements-pending";
    }

    /** Innesca il caso d'uso esistente di accettazione, poi torna all'elenco accordi. */
    @PostMapping("/{id}/accept")
    public String accept(@PathVariable UUID id) {
        lifecycleHandler.accept(id);
        return "redirect:/ui/agreements";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id) {
        lifecycleHandler.reject(id);
        return "redirect:/ui/agreements";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id) {
        lifecycleHandler.cancel(id);
        return "redirect:/ui/agreements";
    }

    /**
     * L'accordo referenzia CPO ed eMSP tramite il solo UUID: queste mappe permettono ai
     * template di mostrare anche nome e identificativo esterno, senza denormalizzarli
     * dentro {@code AgreementResponse}.
     */
    private void addActorLookups(Model model) {
        List<CpoResponse> cpos = cpoQueryHandler.findAll().stream()
                .map(CpoPersistenceMapper::toResponse)
                .toList();
        List<EmspResponse> emsps = emspQueryHandler.search(null, null).stream()
                .map(EmspPersistenceMapper::toResponse)
                .toList();

        model.addAttribute("cpoById",
                cpos.stream().collect(Collectors.toMap(CpoResponse::id, Function.identity())));
        model.addAttribute("emspById",
                emsps.stream().collect(Collectors.toMap(EmspResponse::id, Function.identity())));
    }
}
