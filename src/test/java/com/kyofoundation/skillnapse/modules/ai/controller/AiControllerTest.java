package com.kyofoundation.skillnapse.modules.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import(SecurityConfig.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AiOrchestratorService aiOrchestratorService;

    @Test
    @DisplayName("Deve responder 200 OK com o JSON gerado pela IA")
    void deveResponder200ComSucesso() throws Exception {
        PromptRequest request = new PromptRequest("Explique polimorfismo", "Você é um tutor", 0.7, 300, Map.of());
        AiGenerationResponse response = new AiGenerationResponse(
                "Polimorfismo é a capacidade de um objeto assumir muitas formas.",
                "gemini",
                "default",
                10L,
                25L,
                35L,
                Instant.now()
        );

        when(aiOrchestratorService.generate(any(PromptRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Polimorfismo é a capacidade de um objeto assumir muitas formas."))
                .andExpect(jsonPath("$.provider").value("gemini"));
    }
}
