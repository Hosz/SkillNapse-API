package com.kyofoundation.skillnapse.modules.questao.dto.response;

import java.time.Instant;
import java.util.UUID;

public record HistoricoTentativaResponse(
        UUID id,
        UUID questaoId,
        String enunciadoQuestao,
        String assuntoGeral,
        String topicoReferencia,
        UUID alternativaEscolhidaId,
        String letraEscolhida,
        Boolean acertou,
        Integer tempoGastoSegundos,
        UUID simuladoId,
        Instant respondidoEm
) {
}
