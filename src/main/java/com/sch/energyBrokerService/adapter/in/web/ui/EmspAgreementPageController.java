package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.CpoResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.application.service.agreement.command.AgreementLifecycleCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import lombok.RequiredArgsConstructor;

/**
 * Pagina dell'attore eMSP: visualizza gli accordi proposti dai CPO e li accetta (o rifiuta).
 *
 * <p>Riusa i casi d'uso esistenti: {@link AgreementQueryHandler} filtrato su
 * {@link AgreementStatus#PROPOSED} e {@link AgreementLifecycleCommandHandler} per la
 * transizione di stato. La proposta NON è disponibile qui: è di competenza del CPO.
 */
@Controller
@Profile("dev")
@RequestMapping("/ui/emsp/agreements")
@RequiredArgsConstructor
public class EmspAgreementPageController {

    private final CpoQueryHandler cpoQueryHandler;
    private final EmspQueryHandler emspQueryHandler;
    private final AgreementQueryHandler agreementQueryHandler;
    private final AgreementLifecycleCommandHandler lifecycleHandler;

    @GetMapping
    public String inbox(@RequestParam(required = false) UUID emspId, Model model) {
        List<CpoResponse> cpos = cpoQueryHandler.findAll().stream()
                .map(CpoPersistenceMapper::toResponse)
                .toList();

        model.addAttribute("emsps", emspQueryHandler.search(null, null).stream()
                .map(EmspPersistenceMapper::toResponse)
                .toList());
        model.addAttribute("selectedEmspId", emspId);
        model.addAttribute("agreements",
                agreementQueryHandler.search(null, emspId, AgreementStatus.PROPOSED).stream()
                        .map(AgreementPersistenceMapper::toResponse)
                        .toList());
        model.addAttribute("cpoById", byId(cpos));
        return "agreement-accept-emsp";
    }

    /** L'eMSP accetta: si riusa la transizione di stato dell'aggregato Agreement. */
    @PostMapping("/{id}/accept")
    public String accept(@PathVariable UUID id,
                         @RequestParam(required = false) UUID emspId) {
        lifecycleHandler.accept(id);
        return redirectToInbox(emspId);
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id,
                         @RequestParam(required = false) UUID emspId) {
        lifecycleHandler.reject(id);
        return redirectToInbox(emspId);
    }

    private static String redirectToInbox(UUID emspId) {
        return emspId == null
                ? "redirect:/ui/emsp/agreements"
                : "redirect:/ui/emsp/agreements?emspId=" + emspId;
    }

    private static Map<UUID, CpoResponse> byId(List<CpoResponse> cpos) {
        return cpos.stream().collect(
                Collectors.toMap(CpoResponse::id, Function.identity()));
    }
}
