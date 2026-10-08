package com.kyofoundation.skillnapse.modules.flashcard.dto.response;

import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Registro de uma execução de revisão de flashcard")
public record HistoricoRevisaoResponse(

        @Schema(description = "Identificador único do registro de revisão", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "ID do flashcard revisado", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        UUID flashcardId,

        @Schema(description = "Classificação atribuída pelo usuário", example = "BOM")
        ClassificacaoResposta classificacaoResposta,

        @Schema(description = "Tempo gasto para resposta em segundos", example = "7")
        Integer tempoRespostaSegundos,

        @Schema(description = "Timestamp UTC de quando a revisão foi submetida", example = "2026-10-08T12:00:00Z")
        Instant revisadoEm
) {
}
