package com.kyofoundation.skillnapse.modules.flashcard.dto.request;

import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload para submissão de revisão ativa de um flashcard")
public record RevisarFlashcardRequest(

        @NotNull(message = "A classificação da resposta é obrigatória (ERRO, DIFICIL, BOM, FACIL).")
        @Schema(description = "Avaliação subjetiva da retenção do card", example = "BOM")
        ClassificacaoResposta classificacao,

        @Min(value = 0, message = "O tempo de resposta não pode ser negativo.")
        @Schema(description = "Tempo total em segundos gasto para responder o card", example = "8")
        Integer tempoRespostaSegundos
) {
}
