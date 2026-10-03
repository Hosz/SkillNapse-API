package com.kyofoundation.skillnapse.modules.ai.service;

import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.validator.AiPromptValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AiOrchestratorService {

    private final ChatClient.Builder chatClientBuilder;
    private final AiPromptValidator aiPromptValidator;

    @Value("${skillnapse.ai.provider:gemini}")
    private String activeProvider = "gemini";

    public AiGenerationResponse generate(PromptRequest request) {
        aiPromptValidator.validatePromptRequest(request);

        ChatClient chatClient = chatClientBuilder.build();
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(request.prompt());

        if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
            spec = spec.system(request.systemMessage());
        }

        String content = spec.call().content();
        return new AiGenerationResponse(content, activeProvider, "default", 0L, 0L, 0L, Instant.now());
    }

    public <T> T generateStructured(PromptRequest request, Class<T> responseType) {
        aiPromptValidator.validateStructuredRequest(request, responseType);

        ChatClient chatClient = chatClientBuilder.build();
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(request.prompt());

        if (request.systemMessage() != null && !request.systemMessage().isBlank()) {
            spec = spec.system(request.systemMessage());
        }

        return spec.call().entity(responseType);
    }

    public String getActiveProviderName() {
        return activeProvider;
    }
}
