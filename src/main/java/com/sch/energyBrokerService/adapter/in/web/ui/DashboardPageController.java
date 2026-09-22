package com.sch.energyBrokerService.adapter.in.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sch.energyBrokerService.application.service.agreement.query.AgreementQueryHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;
import com.sch.energyBrokerService.domain.model.agreement.AgreementStatus;

import lombok.RequiredArgsConstructor;

/**
 * Console di sviluppo/demo: dashboard con i totali del dominio.
 * Riusa i query handler applicativi, nessuna logica di business duplicata.
 */
@Controller
@RequiredArgsConstructor
public class DashboardPageController {

    private final CpoQueryHandler cpoQueryHandler;
    private final EmspQueryHandler emspQueryHandler;
    private final AgreementQueryHandler agreementQueryHandler;

    @GetMapping({"/", "/ui"})
    public String dashboard(Model model) {
        model.addAttribute("cpoCount", cpoQueryHandler.findAll().size());
        model.addAttribute("emspCount", emspQueryHandler.search(null, null).size());
        model.addAttribute("agreementCount",
                agreementQueryHandler.search(null, null, null).size());
        model.addAttribute("proposedCount",
                agreementQueryHandler.search(null, null, AgreementStatus.PROPOSED).size());
        model.addAttribute("acceptedCount",
                agreementQueryHandler.search(null, null, AgreementStatus.ACCEPTED).size());
        return "dashboard";
    }
}
