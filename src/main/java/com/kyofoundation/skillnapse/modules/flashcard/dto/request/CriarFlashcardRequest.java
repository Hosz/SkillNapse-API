package com.kyofoundation.skillnapse.modules.flashcard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Payload para criação de um novo flashcard")
public record CriarFlashcardRequest(

        @NotNull(message = "O ID do baralho é obrigatório.")
        @Schema(description = "ID do baralho ao qual o flashcard pertence", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID baralhoId,

        @Schema(description = "ID do tópico ao qual o flashcard está acoplado (opcional)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID topicoId,

        @NotBlank(message = "O texto da frente (pergunta/conceito) é obrigatório.")
        @Schema(description = "Conteúdo exibido na frente do cartão", example = "O que é o princípio da simetria constitucional?")
        String frente,

        @NotBlank(message = "O texto do verso (resposta/explicação) é obrigatório.")
        @Schema(description = "Conteúdo exibido no verso do cartão", example = "Obrigação de reprodução de regras e princípios da CF pelos estados e municípios.")
        String verso
) {
}
