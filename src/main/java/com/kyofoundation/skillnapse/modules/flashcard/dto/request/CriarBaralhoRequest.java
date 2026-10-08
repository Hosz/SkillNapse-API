package com.kyofoundation.skillnapse.modules.flashcard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Payload para criação de um novo baralho de flashcards")
public record CriarBaralhoRequest(

        @NotBlank(message = "O título do baralho é obrigatório.")
        @Size(max = 150, message = "O título do baralho não pode ultrapassar 150 caracteres.")
        @Schema(description = "Título descritivo do baralho", example = "Direito Constitucional - Teoria Geral")
        String titulo,

        @Schema(description = "Descrição ou observações opcionais sobre o conteúdo do baralho", example = "Cards voltados para fixação dos artigos 1º a 17 da CF/88.")
        String descricao,

        @Schema(description = "ID da matéria vinculada (opcional)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID materiaId
) {
}
