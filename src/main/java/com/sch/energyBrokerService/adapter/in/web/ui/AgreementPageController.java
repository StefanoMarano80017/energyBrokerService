package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ui/agreements")
@RequiredArgsConstructor
public class AgreementPageController {

    private final AgreementQueryHandler agreementQueryHandler;
    private final AgreementLifecycleCommandHandler lifecycleHandler;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("agreements", agreementQueryHandler.search(null, null, null).stream()
                .map(AgreementPersistenceMapper::toResponse)
                .toList());
        return "agreements";
    }

    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("agreements",
                agreementQueryHandler.search(null, null, AgreementStatus.PROPOSED).stream()
                        .map(AgreementPersistenceMapper::toResponse)
                        .toList());
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
}
