package com.kyofoundation.skillnapse.modules.questao.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ResultadoResolucaoResponse(
        UUID tentativaId,
        UUID questaoId,
        UUID alternativaEscolhidaId,
        String letraEscolhida,
        Boolean acertou,
        UUID alternativaCorretaId,
        String letraCorreta,
        String explicacaoGabarito,
        Integer tempoGastoSegundos,
        Instant respondidoEm
) {
}
