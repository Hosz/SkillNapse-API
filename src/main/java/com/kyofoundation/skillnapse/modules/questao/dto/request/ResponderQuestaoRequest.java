package com.kyofoundation.skillnapse.modules.questao.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResponderQuestaoRequest(
        @NotNull(message = "O ID da alternativa escolhida é obrigatório.")
        UUID alternativaEscolhidaId,

        @Min(value = 0, message = "O tempo gasto em segundos deve ser maior ou igual a zero.")
        Integer tempoGastoSegundos,

        UUID topicoId,

        UUID simuladoId
) {
}
