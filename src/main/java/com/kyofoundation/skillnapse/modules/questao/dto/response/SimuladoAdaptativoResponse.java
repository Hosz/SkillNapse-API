package com.kyofoundation.skillnapse.modules.questao.dto.response;

import com.kyofoundation.skillnapse.modules.questao.enums.TipoSimulado;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SimuladoAdaptativoResponse(
        UUID simuladoId,
        String titulo,
        TipoSimulado tipo,
        Boolean concluido,
        Instant criadoEm,
        Integer totalQuestoes,
        List<QuestaoItemSimuladoResponse> questoes
) {
}
