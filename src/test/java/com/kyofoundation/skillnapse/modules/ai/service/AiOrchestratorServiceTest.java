package com.kyofoundation.skillnapse.modules.ai.service;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.validator.AiPromptValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiOrchestratorServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatModel chatModel;

    private AiPromptValidator validator;
    private AiOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        validator = new AiPromptValidator();
        orchestratorService = new AiOrchestratorService(chatClientBuilder, chatModel, validator);
    }

    record StructuredPlanResponse(String materia, List<String> topicos) {}

    @Test
    @DisplayName("Deve gerar resposta textual chamando diretamente a classe ChatClient")
    void deveGerarRespostaTextualComSucesso() {
        PromptRequest request = new PromptRequest("Explique o princípio SRP", "Você é um mentor");

        org.springframework.ai.chat.model.ChatResponse mockChatResponse = org.mockito.Mockito.mock(
                org.springframework.ai.chat.model.ChatResponse.class,
                Answers.RETURNS_DEEP_STUBS
        );
        when(mockChatResponse.getResult().getOutput().getText())
                .thenReturn("Single Responsibility Principle diz que uma classe deve ter apenas um motivo para mudar.");
        when(mockChatResponse.getMetadata().getModel()).thenReturn("gemini-2.5-flash");
        when(mockChatResponse.getMetadata().getUsage().getPromptTokens()).thenReturn(10);
        when(mockChatResponse.getMetadata().getUsage().getCompletionTokens()).thenReturn(20);
        when(mockChatResponse.getMetadata().getUsage().getTotalTokens()).thenReturn(30);

        when(chatClientBuilder.build().prompt().user(request.prompt()).system(request.systemMessage()).call().chatResponse())
                .thenReturn(mockChatResponse);

        AiGenerationResponse response = orchestratorService.generate(request);

        assertThat(response).isNotNull();
        assertThat(response.content()).contains("Single Responsibility Principle");
        assertThat(response.model()).isEqualTo("gemini-2.5-flash");
        assertThat(response.promptTokens()).isEqualTo(10L);
        assertThat(response.generationTokens()).isEqualTo(20L);
        assertThat(response.totalTokens()).isEqualTo(30L);
        assertThat(response.provider()).isEqualTo("gemini");
    }

    @Test
    @DisplayName("Deve gerar resposta estruturada em JSON/Record diretamente via ChatClient")
    void deveGerarRespostaEstruturadaComSucesso() {
        PromptRequest request = new PromptRequest("Gere matéria e tópicos");
        StructuredPlanResponse expected = new StructuredPlanResponse(
                "Direito Constitucional",
                List.of("Direitos Fundamentais", "Poder Executivo")
        );

        when(chatClientBuilder.build().prompt().user(request.prompt()).call().entity(StructuredPlanResponse.class))
                .thenReturn(expected);

        StructuredPlanResponse result = orchestratorService.generateStructured(request, StructuredPlanResponse.class);

        assertThat(result).isNotNull();
        assertThat(result.materia()).isEqualTo("Direito Constitucional");
        assertThat(result.topicos()).hasSize(2).contains("Direitos Fundamentais");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException e interromper chamada ao ChatClient quando prompt for inválido")
    void deveLancarExcecaoEInterromperChamadaAoChatClient() {
        PromptRequest invalidRequest = new PromptRequest("   ");

        assertThatThrownBy(() -> orchestratorService.generate(invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O prompt não pode ser nulo ou vazio.");

        verify(chatClientBuilder, never()).build();
    }

    @Test
    @DisplayName("Deve aplicar opções de temperatura e maxTokens quando fornecidas na requisição")
    void deveAplicarOpcoesDeTemperaturaEMaxTokens() {
        PromptRequest request = new PromptRequest("Explique recursão", null, 0.7, 500, null);

        ChatResponse mockChatResponse = org.mockito.Mockito.mock(ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
        when(mockChatResponse.getResult().getOutput().getText()).thenReturn("Recursão...");

        when(chatClientBuilder.build().prompt().user(request.prompt()).options(org.mockito.ArgumentMatchers.any(org.springframework.ai.chat.prompt.ChatOptions.Builder.class)).call().chatResponse())
                .thenReturn(mockChatResponse);

        AiGenerationResponse response = orchestratorService.generate(request);

        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("Recursão...");
    }

    @Test
    @DisplayName("Deve retornar provedor configurado explicitamente via property")
    void deveRetornarProvedorConfiguradoExplicitamente() {
        org.springframework.test.util.ReflectionTestUtils.setField(orchestratorService, "configuredProvider", "openai");
        assertThat(orchestratorService.getActiveProviderName()).isEqualTo("openai");
    }
}
