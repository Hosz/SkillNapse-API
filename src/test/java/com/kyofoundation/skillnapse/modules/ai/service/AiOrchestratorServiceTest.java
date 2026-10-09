package com.kyofoundation.skillnapse.modules.ai.service;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.validator.AiPromptValidator;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiOrchestratorServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatModel chatModel;

    @Mock
    private UserFinder userFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    private AiPromptValidator validator;
    private AiOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        validator = new AiPromptValidator();
        orchestratorService = new AiOrchestratorService(chatClientBuilder, chatModel, validator, userFinder, usuarioValidator);
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
        assertThat(response.content()).isEqualTo("Single Responsibility Principle diz que uma classe deve ter apenas um motivo para mudar.");
        assertThat(response.model()).isEqualTo("gemini-2.5-flash");
        assertThat(response.promptTokens()).isEqualTo(10L);
        assertThat(response.generationTokens()).isEqualTo(20L);
        assertThat(response.totalTokens()).isEqualTo(30L);
    }

    @Test
    @DisplayName("Deve validar usuário ativo ao chamar generate com userId")
    void deveValidarUsuarioAtivoComUserId() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PromptRequest request = new PromptRequest("Prompt teste");

        when(userFinder.findById(userId)).thenReturn(usuario);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);

        org.springframework.ai.chat.model.ChatResponse mockChatResponse = org.mockito.Mockito.mock(
                org.springframework.ai.chat.model.ChatResponse.class,
                Answers.RETURNS_DEEP_STUBS
        );
        when(mockChatResponse.getResult().getOutput().getText()).thenReturn("Resposta teste");
        when(chatClientBuilder.build().prompt().user(request.prompt()).call().chatResponse()).thenReturn(mockChatResponse);

        AiGenerationResponse response = orchestratorService.generate(userId, request);

        assertThat(response).isNotNull();
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando usuário inativo chamar generate")
    void deveLancarForbiddenComUsuarioInativo() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).ativo(false).build();
        PromptRequest request = new PromptRequest("Prompt teste");

        when(userFinder.findById(userId)).thenReturn(usuario);
        doThrow(new ForbiddenException("Usuário inativo ou bloqueado no sistema."))
                .when(usuarioValidator).validarUsuarioAtivo(usuario);

        assertThatThrownBy(() -> orchestratorService.generate(userId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Usuário inativo ou bloqueado no sistema.");
    }

    @Test
    @DisplayName("Deve lançar exceção BadRequestException quando prompt for nulo ou em branco")
    void deveLancarExcecaoQuandoPromptForVazio() {
        PromptRequest requestInvalido = new PromptRequest("   ");

        assertThatThrownBy(() -> orchestratorService.generate(requestInvalido))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O prompt não pode ser nulo ou vazio.");

        verify(chatClientBuilder, never()).build();
    }
}
