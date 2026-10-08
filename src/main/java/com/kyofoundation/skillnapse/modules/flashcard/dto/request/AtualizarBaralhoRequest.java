package com.kyofoundation.skillnapse.modules.flashcard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Payload para atualização de um baralho existente")
public record AtualizarBaralhoRequest(

        @NotBlank(message = "O título do baralho é obrigatório.")
        @Size(max = 150, message = "O título do baralho não pode ultrapassar 150 caracteres.")
        @Schema(description = "Novo título do baralho", example = "Direito Constitucional - Atualizado")
        String titulo,

        @Schema(description = "Nova descrição do baralho", example = "Foco em jurisprudência recente do STF.")
        String descricao,

        @Schema(description = "ID da matéria vinculada (opcional)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID materiaId
) {
}
