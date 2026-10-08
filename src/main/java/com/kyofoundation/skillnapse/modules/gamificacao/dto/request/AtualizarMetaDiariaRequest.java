package com.kyofoundation.skillnapse.modules.gamificacao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AtualizarMetaDiariaRequest(
        @NotNull(message = "A meta de minutos de estudo é obrigatória.")
        @Positive(message = "A meta de minutos de estudo deve ser maior que zero.")
        @Schema(description = "Meta diária de estudo líquido em minutos", example = "120")
        Integer metaMinutosEstudo,

        @NotNull(message = "A meta de questões resolvidas é obrigatória.")
        @PositiveOrZero(message = "A meta de questões resolvidas não pode ser negativa.")
        @Schema(description = "Meta diária de questões resolvidas", example = "15")
        Integer metaQuestoesResolvidas
) {
}
