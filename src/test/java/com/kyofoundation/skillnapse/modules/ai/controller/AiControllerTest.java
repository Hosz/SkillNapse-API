package com.kyofoundation.skillnapse.modules.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AiOrchestratorService aiOrchestratorService;

    @Test
    @DisplayName("Deve responder 200 OK com o JSON gerado pela IA")
    void deveResponder200ComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
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

        when(aiOrchestratorService.generate(eq(userId), any(PromptRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/generate")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Polimorfismo é a capacidade de um objeto assumir muitas formas."))
                .andExpect(jsonPath("$.provider").value("gemini"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized ao tentar acessar endpoint de IA sem autenticação")
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        PromptRequest request = new PromptRequest("Explique polimorfismo");

        mockMvc.perform(post("/api/v1/ai/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando JSON enviado for malformado")
    void deveRetornar400QuandoJsonForMalformado() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/ai/generate")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed json syntax"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("O corpo da requisição é inválido ou está malformado."));
    }

    @Test
    @DisplayName("Deve retornar 500 com mensagem genérica quando ocorrer erro inesperado sem vazar detalhes internos")
    void deveRetornar500ComMensagemGenericaSemVazarDetalhesInternos() throws Exception {
        UUID userId = UUID.randomUUID();
        PromptRequest request = new PromptRequest("Explique polimorfismo");
        when(aiOrchestratorService.generate(eq(userId), any(PromptRequest.class)))
                .thenThrow(new RuntimeException("Database connection timeout: postgresql://secret-host:5432"));

        mockMvc.perform(post("/api/v1/ai/generate")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde."));
    }
}
