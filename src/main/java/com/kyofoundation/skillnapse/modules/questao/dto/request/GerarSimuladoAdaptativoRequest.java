package com.kyofoundation.skillnapse.modules.questao.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record GerarSimuladoAdaptativoRequest(
        @NotNull(message = "O ID do plano de estudo é obrigatório.")
        UUID planoEstudoId,

        @Min(value = 3, message = "O simulado adaptativo deve conter no mínimo 3 questões.")
        @Max(value = 15, message = "O simulado adaptativo pode conter no máximo 15 questões por geração.")
        Integer quantidadeQuestoes,

        @Size(max = 50, message = "O nome da banca avaliadora não pode ultrapassar 50 caracteres.")
        String bancaAlvo,

        List<UUID> topicoIds
) {
    public int quantidadeEfetiva() {
        return (quantidadeQuestoes != null && quantidadeQuestoes >= 3) ? quantidadeQuestoes : 5;
    }
}
