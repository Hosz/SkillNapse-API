package com.kyofoundation.skillnapse.modules.redacao.dto.payload;

import java.util.List;

public record FeedbackCorrecaoIaPayload(
        Double notaGeral,
        List<AvaliacaoCompetenciaIaPayload> competencias,
        String comentariosGerais,
        List<SugestaoReescritaIaPayload> sugestoesReescrita
) {
}
