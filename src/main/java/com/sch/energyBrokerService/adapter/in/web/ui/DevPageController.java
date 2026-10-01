package com.sch.energyBrokerService.adapter.in.web.ui;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sch.energyBrokerService.dev.DataInitializer;

import lombok.RequiredArgsConstructor;

@Controller
@Profile("dev")
@RequestMapping("/ui/dev")
@RequiredArgsConstructor
public class DevPageController {

    private final DataInitializer dataInitializer;

    @GetMapping
    public String page() {
        return "dev";
    }

    @PostMapping("/init")
    public String init(Model model) {
        addResult(model, "Dati inizializzati", dataInitializer.init());
        return "dev";
    }

    @PostMapping("/reset")
    public String reset(Model model) {
        addResult(model, "Dati resettati", dataInitializer.reset());
        return "dev";
    }

    private void addResult(Model model, String operation, Map<String, Integer> summary) {
        model.addAttribute("operation", operation);
        model.addAttribute("summary", summary);
    }
}