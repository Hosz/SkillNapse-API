package com.kyofoundation.skillnapse.modules.redacao.dto.payload;

public record AvaliacaoLegibilidadeIaPayload(
        boolean legivel,
        Double percentualLegibilidade,
        String justificativaIlegibilidade,
        String textoTranscrito,
        FeedbackCorrecaoIaPayload correcao
) {
}
