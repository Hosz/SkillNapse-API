package com.kyofoundation.skillnapse.modules.flashcard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representação detalhada de um flashcard e seus metadados de repetição espaçada")
public record FlashcardResponse(

        @Schema(description = "Identificador único do flashcard", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "ID do baralho ao qual o card pertence", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        UUID baralhoId,

        @Schema(description = "Título do baralho", example = "Direito Constitucional")
        String baralhoTitulo,

        @Schema(description = "ID do tópico acoplado (se houver)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID topicoId,

        @Schema(description = "Título do tópico acoplado (se houver)", example = "Direitos Fundamentais")
        String topicoTitulo,

        @Schema(description = "Texto ou pergunta da frente do cartão", example = "Qual é o prazo para impetrar mandado de segurança?")
        String frente,

        @Schema(description = "Texto ou resposta do verso do cartão", example = "120 dias a contar da ciência do ato lesivo.")
        String verso,

        @Schema(description = "Fator de facilidade (Easiness Factor do SM-2)", example = "2.50")
        BigDecimal fatorFacilidade,

        @Schema(description = "Intervalo em dias até a próxima revisão", example = "6")
        Integer intervaloDias,

        @Schema(description = "Contador de revisões consecutivas bem-sucedidas", example = "2")
        Integer repeticoes,

        @Schema(description = "Data agendada para a próxima revisão (formato YYYY-MM-DD)", example = "2026-10-14")
        LocalDate proximaRevisao,

        @Schema(description = "Timestamp UTC da última revisão realizada", example = "2026-10-08T12:00:00Z")
        Instant ultimaRevisao
) {
}
