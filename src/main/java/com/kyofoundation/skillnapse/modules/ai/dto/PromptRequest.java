package com.kyofoundation.skillnapse.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "Dados para envio de prompt ao LLM")
public record PromptRequest(
    @Schema(description = "Instrução principal ou pergunta a ser processada pela IA", example = "Explique o princípio da responsabilidade única (SRP) em dois parágrafos.")
    @NotBlank(message = "O prompt não pode ser nulo ou vazio.")
    String prompt,

    @Schema(description = "Mensagem de contexto de sistema (papel do modelo)", example = "Você é um mentor sênior de Java didático e conciso.")
    String systemMessage,

    @Schema(description = "Criatividade da resposta (entre 0.0 determinístico e 2.0 criativo)", example = "0.7")
    Double temperature,

    @Schema(description = "Quantidade máxima de tokens para resposta", example = "500")
    Integer maxTokens,

    @Schema(description = "Parâmetros e metadados adicionais flexíveis")
    Map<String, Object> metadata
) {
    public PromptRequest(String prompt) {
        this(prompt, null, null, null, Map.of());
    }

    public PromptRequest(String prompt, String systemMessage) {
        this(prompt, systemMessage, null, null, Map.of());
    }

    public PromptRequest(String prompt, String systemMessage, Double temperature, Integer maxTokens) {
        this(prompt, systemMessage, temperature, maxTokens, Map.of());
    }
}
