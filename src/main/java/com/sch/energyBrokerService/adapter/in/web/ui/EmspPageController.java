package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateEmspRequest;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.EmspPersistenceMapper;
import com.sch.energyBrokerService.application.exception.EntityNotFoundException;
import com.sch.energyBrokerService.application.service.emsp.command.EmspCommandHandler;
import com.sch.energyBrokerService.application.service.emsp.query.EmspQueryHandler;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ui/emsps")
@RequiredArgsConstructor
public class EmspPageController {

    private final EmspQueryHandler emspQueryHandler;
    private final EmspCommandHandler emspCommandHandler;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("emsps", emspQueryHandler.search(null).stream()
                .map(EmspPersistenceMapper::toResponse)
                .toList());
        return "emsps";
    }

    @PostMapping
    public String create(@ModelAttribute CreateEmspRequest request) {
        emspCommandHandler.create(request);
        return "redirect:/ui/emsps";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var emsp = emspQueryHandler.findById(id)
                .map(EmspPersistenceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Emsp not found: " + id));
        model.addAttribute("emsp", emsp);
        return "emsp-edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable UUID id, @ModelAttribute CreateEmspRequest request) {
        emspCommandHandler.update(id, request);
        return "redirect:/ui/emsps";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        emspCommandHandler.delete(id);
        return "redirect:/ui/emsps";
    }
}
