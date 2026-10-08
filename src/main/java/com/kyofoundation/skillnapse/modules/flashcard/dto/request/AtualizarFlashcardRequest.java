package com.kyofoundation.skillnapse.modules.flashcard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Payload para atualização de um flashcard existente")
public record AtualizarFlashcardRequest(

        @Schema(description = "ID do tópico ao qual o flashcard está acoplado (opcional)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID topicoId,

        @NotBlank(message = "O texto da frente (pergunta/conceito) é obrigatório.")
        @Schema(description = "Novo conteúdo da frente do cartão", example = "O que estabelece a cláusula pétrea?")
        String frente,

        @NotBlank(message = "O texto do verso (resposta/explicação) é obrigatório.")
        @Schema(description = "Novo conteúdo do verso do cartão", example = "Dispositivos constitucionais imutáveis que não podem ser abolidos por PEC.")
        String verso
) {
}
