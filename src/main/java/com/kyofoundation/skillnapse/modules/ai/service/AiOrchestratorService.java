package com.kyofoundation.skillnapse.modules.ai.service;

import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.validator.AiPromptValidator;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiOrchestratorService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatModel chatModel;
    private final AiPromptValidator aiPromptValidator;
    private final UserFinder userFinder;
    private final UsuarioValidator usuarioValidator;

    @Value("${skillnapse.ai.provider:}")
    private String configuredProvider;

    public AiGenerationResponse generate(UUID userId, PromptRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);
        return generate(request);
    }

    public AiGenerationResponse generate(PromptRequest request) {
        aiPromptValidator.validatePromptRequest(request);

        ChatClient chatClient = chatClientBuilder.build();
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(request.prompt());
        spec = applyPromptOptions(spec, request);

        ChatResponse chatResponse = spec.call().chatResponse();

        String content = "";
        String model = "unknown";
        Long promptTokens = 0L;
        Long generationTokens = 0L;
        Long totalTokens = 0L;

        if (chatResponse != null) {
            if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                content = chatResponse.getResult().getOutput().getText();
            }
            if (chatResponse.getMetadata() != null) {
                if (chatResponse.getMetadata().getModel() != null) {
                    model = chatResponse.getMetadata().getModel();
                }
                Usage usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens().longValue() : 0L;
                    generationTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens().longValue() : 0L;
                    totalTokens = usage.getTotalTokens() != null ? usage.getTotalTokens().longValue() : (promptTokens + generationTokens);
                }
            }
        }

        return new AiGenerationResponse(
                content != null ? content : "",
                getActiveProviderName(),
                model,
                promptTokens,
                generationTokens,
                totalTokens,
                Instant.now()
        );
    }

    public <T> T generateStructured(PromptRequest request, Class<T> responseType) {
        aiPromptValidator.validateStructuredRequest(request, responseType);

        ChatClient chatClient = chatClientBuilder.build();
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(request.prompt());
        spec = applyPromptOptions(spec, request);

        return spec.call().entity(responseType);
    }

    public <T> T generateStructuredMultimodal(
            PromptRequest request,
            org.springframework.util.MimeType mimeType,
            org.springframework.core.io.Resource mediaResource,
            Class<T> responseType
    ) {
        aiPromptValidator.validateStructuredRequest(request, responseType);

        ChatClient chatClient = chatClientBuilder.build();
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                .user(userSpec -> userSpec.text(request.prompt()).media(mimeType, mediaResource));
        spec = applyPromptOptions(spec, request);

        return spec.call().entity(responseType);
    }

    public String getActiveProviderName() {
        if (configuredProvider != null && !configuredProvider.isBlank()) {
            return configuredProvider;
        }
        if (chatModel != null) {
            String className = chatModel.getClass().getSimpleName().toLowerCase();
            if (className.contains("google") || className.contains("gemini")) {
                return "gemini";
            }
            if (className.contains("openai")) {
                return "openai";
            }
            if (className.contains("ollama")) {
                return "ollama";
            }
        }
        return "gemini";
    }

    private ChatClient.ChatClientRequestSpec applyPromptOptions(ChatClient.ChatClientRequestSpec spec, PromptRequest request) {
        if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
            spec = spec.system(request.systemMessage());
        }

        ChatOptions.Builder<?> optionsBuilder = ChatOptions.builder();
        boolean hasOptions = false;

        if (request.temperature() != null) {
            optionsBuilder = optionsBuilder.temperature(request.temperature());
            hasOptions = true;
        }

        if (request.maxTokens() != null) {
            optionsBuilder = optionsBuilder.maxTokens(request.maxTokens());
            hasOptions = true;
        }

        if (hasOptions) {
            spec = spec.options(optionsBuilder);
        }

        return spec;
    }
}
