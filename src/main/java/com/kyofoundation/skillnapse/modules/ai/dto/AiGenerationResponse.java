package com.kyofoundation.skillnapse.modules.ai.dto;

import java.time.Instant;

public record AiGenerationResponse(
    String content,
    String provider,
    String model,
    Long promptTokens,
    Long generationTokens,
    Long totalTokens,
    Instant timestamp
) {
    public static AiGenerationResponse of(String content, String provider, String model) {
        return new AiGenerationResponse(content, provider, model, 0L, 0L, 0L, Instant.now());
    }
}
