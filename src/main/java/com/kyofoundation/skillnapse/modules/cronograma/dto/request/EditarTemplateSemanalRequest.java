package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record EditarTemplateSemanalRequest(
        @Size(max = 100, message = "O nome do template não pode exceder 100 caracteres.")
        @Schema(description = "Novo nome descritivo do template", example = "Rotina Intensiva 2026")
        String nome,

        @Schema(description = "Status de ativação do template", example = "true")
        Boolean ativo
) {
}
