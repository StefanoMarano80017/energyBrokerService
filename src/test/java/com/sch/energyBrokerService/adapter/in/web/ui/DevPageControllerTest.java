package com.sch.energyBrokerService.adapter.in.web.ui;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sch.energyBrokerService.dev.DataInitializer;

import static org.mockito.BDDMockito.given;

@WebMvcTest(DevPageController.class)
@ActiveProfiles("dev")
class DevPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataInitializer dataInitializer;

    @Test
    void showsDevelopmentDataPage() throws Exception {
        mockMvc.perform(get("/ui/dev"))
                .andExpect(status().isOk())
                .andExpect(view().name("dev"));
    }

    @Test
    void initializesData() throws Exception {
        given(dataInitializer.init()).willReturn(Map.of("cpos", 1));

        mockMvc.perform(post("/ui/dev/init"))
                .andExpect(status().isOk())
                .andExpect(view().name("dev"));

        verify(dataInitializer).init();
    }

    @Test
    void resetsData() throws Exception {
        given(dataInitializer.reset()).willReturn(Map.of("cpos", 0));

        mockMvc.perform(post("/ui/dev/reset"))
                .andExpect(status().isOk())
                .andExpect(view().name("dev"));

        verify(dataInitializer).reset();
    }
}