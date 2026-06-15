package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateCpoRequest;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.CpoPersistenceMapper;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.cpo.command.CpoCommandHandler;
import com.sch.energyBrokerService.application.service.cpo.query.CpoQueryHandler;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ui/cpos")
@RequiredArgsConstructor
public class CpoPageController {

    private final CpoQueryHandler cpoQueryHandler;
    private final CpoCommandHandler cpoCommandHandler;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("cpos", cpoQueryHandler.findAll().stream()
                .map(CpoPersistenceMapper::toResponse)
                .toList());
        return "cpos";
    }

    @PostMapping
    public String create(@ModelAttribute CreateCpoRequest request) {
        cpoCommandHandler.create(request);
        return "redirect:/ui/cpos";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var cpo = cpoQueryHandler.findById(id)
                .map(CpoPersistenceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cpo not found: " + id));
        model.addAttribute("cpo", cpo);
        return "cpo-edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable UUID id, @ModelAttribute CreateCpoRequest request) {
        cpoCommandHandler.update(id, request);
        return "redirect:/ui/cpos";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        cpoCommandHandler.delete(id);
        return "redirect:/ui/cpos";
    }
}
