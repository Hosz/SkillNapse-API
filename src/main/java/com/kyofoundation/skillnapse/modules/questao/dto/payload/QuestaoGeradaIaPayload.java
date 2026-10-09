package com.kyofoundation.skillnapse.modules.questao.dto.payload;

import java.util.List;

public record QuestaoGeradaIaPayload(
        String enunciado,
        String explicacaoGabarito,
        String dificuldade,
        List<AlternativaGeradaIaPayload> alternativas
) {
}
