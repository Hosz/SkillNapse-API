package com.kyofoundation.skillnapse.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta gerada pelo provedor de inteligência artificial")
public record AiGenerationResponse(
    @Schema(description = "Conteúdo textual ou JSON retornado pela IA", example = "O Princípio da Responsabilidade Única (SRP) estabelece que...")
    String content,

    @Schema(description = "Nome do provedor que processou a requisição", example = "gemini")
    String provider,

    @Schema(description = "Identificador do modelo utilizado", example = "gemini-flash-latest")
    String model,

    @Schema(description = "Quantidade de tokens consumidos no prompt de entrada", example = "15")
    Long promptTokens,

    @Schema(description = "Quantidade de tokens gerados na resposta", example = "120")
    Long generationTokens,

    @Schema(description = "Total de tokens computados", example = "135")
    Long totalTokens,

    @Schema(description = "Momento UTC em que a geração foi concluída")
    Instant timestamp
) {
    public static AiGenerationResponse of(String content, String provider, String model) {
        return new AiGenerationResponse(content, provider, model, 0L, 0L, 0L, Instant.now());
    }
}
