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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateAgreementRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.AgreementResponse;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.EmspResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.AgreementPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.application.service.agreement.command.PublishAgreementCommandHandler;
import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

import lombok.RequiredArgsConstructor;

/**
 * Pagina dell'attore CPO: propone un accordo (una tariffa) verso un eMSP.
 *
 * <p>Riusa i casi d'uso esistenti: {@link PublishAgreementCommandHandler} per la proposta
 * (l'accordo nasce in stato PROPOSED) e {@link AgreementQueryHandler} per la lista degli
 * accordi già proposti dal CPO selezionato. L'accettazione NON è disponibile qui: è di
 * competenza dell'eMSP.
 */
@Controller
@Profile("dev")
@RequestMapping("/ui/cpo/agreements")
@RequiredArgsConstructor
public class CpoAgreementPageController {

    private final CpoQueryHandler cpoQueryHandler;
    private final EmspQueryHandler emspQueryHandler;
    private final AgreementQueryHandler agreementQueryHandler;
    private final PublishAgreementCommandHandler publishHandler;

    @GetMapping
    public String proposeForm(@RequestParam(required = false) UUID cpoId, Model model) {
        List<EmspResponse> emsps = emspQueryHandler.search(null, null).stream()
                .map(EmspPersistenceMapper::toResponse)
                .toList();

        List<AgreementResponse> agreements = agreementQueryHandler.search(cpoId, null, null).stream()
                .map(AgreementPersistenceMapper::toResponse)
                .toList();

        model.addAttribute("cpos", cpoQueryHandler.findAll().stream()
                .map(CpoPersistenceMapper::toResponse)
                .toList());
        model.addAttribute("emsps", emsps);
        model.addAttribute("tariffTypes", TariffType.values());
        model.addAttribute("selectedCpoId", cpoId);
        model.addAttribute("agreements", agreements);
        model.addAttribute("emspById", byId(emsps));
        return "agreement-propose-cpo";
    }

    /** Il CPO propone: si riusa il caso d'uso di pubblicazione, poi POST-redirect-GET. */
    @PostMapping
    public String propose(@ModelAttribute CreateAgreementRequest request) {
        publishHandler.handle(request);
        return "redirect:/ui/cpo/agreements?cpoId=" + request.cpoId();
    }

    private static Map<UUID, EmspResponse> byId(List<EmspResponse> emsps) {
        return emsps.stream().collect(
                Collectors.toMap(EmspResponse::id, Function.identity()));
    }
}
