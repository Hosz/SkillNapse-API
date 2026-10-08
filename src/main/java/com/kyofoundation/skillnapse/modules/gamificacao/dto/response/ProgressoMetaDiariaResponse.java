package com.kyofoundation.skillnapse.modules.gamificacao.dto.response;

import java.time.LocalDate;

public record ProgressoMetaDiariaResponse(
        LocalDate data,
        int metaMinutosEstudo,
        long minutosEstudadosHoje,
        double percentualMinutosEstudo,
        boolean metaMinutosAtingida,
        int metaQuestoesResolvidas,
        long questoesResolvidasHoje,
        double percentualQuestoes,
        boolean metaQuestoesAtingida,
        boolean todasMetasAtingidas
) {
}
