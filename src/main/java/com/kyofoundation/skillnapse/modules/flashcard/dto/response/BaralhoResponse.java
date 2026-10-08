package com.kyofoundation.skillnapse.modules.flashcard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representação detalhada de um baralho de flashcards")
public record BaralhoResponse(

        @Schema(description = "Identificador único do baralho", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "ID do usuário proprietário do baralho", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        UUID usuarioId,

        @Schema(description = "ID da matéria vinculada", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID materiaId,

        @Schema(description = "Nome da matéria vinculada", example = "Direito Constitucional")
        String materiaNome,

        @Schema(description = "Título do baralho", example = "Teoria Geral e Artigo 5º")
        String titulo,

        @Schema(description = "Descrição detalhada do baralho", example = "Cards conceituais sobre direitos individuais e coletivos.")
        String descricao,

        @Schema(description = "Data/hora UTC de criação do baralho", example = "2026-10-08T12:00:00Z")
        Instant criadoEm,

        @Schema(description = "Total de flashcards contidos no baralho", example = "42")
        Long totalCards,

        @Schema(description = "Total de flashcards com revisão pendente para hoje ou atrasados", example = "15")
        Long totalCardsParaRevisar
) {
}
