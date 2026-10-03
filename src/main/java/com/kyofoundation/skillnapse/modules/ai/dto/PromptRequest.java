package com.kyofoundation.skillnapse.modules.ai.dto;

import java.util.Map;

public record PromptRequest(
    String prompt,
    String systemMessage,
    Double temperature,
    Integer maxTokens,
    Map<String, Object> metadata
) {
    public PromptRequest(String prompt) {
        this(prompt, null, null, null, Map.of());
    }

    public PromptRequest(String prompt, String systemMessage) {
        this(prompt, systemMessage, null, null, Map.of());
    }
}
