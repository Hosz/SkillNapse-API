package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarTemplateSemanalRequest(
        @NotBlank(message = "O nome do template semanal não pode estar em branco.")
        @Size(max = 100, message = "O nome do template semanal não pode ultrapassar 100 caracteres.")
        @Schema(description = "Nome descritivo do template semanal", example = "Rotina Padrão 2026")
        String nome,

        @Schema(description = "Define se o template será ativado imediatamente", example = "true")
        Boolean ativo
) {
}
