package com.kyofoundation.skillnapse.modules.questao.dto.response;

import com.kyofoundation.skillnapse.modules.questao.enums.TipoSimulado;

import java.time.Instant;
import java.util.UUID;

public record SimuladoResponse(
        UUID id,
        String titulo,
        TipoSimulado tipo,
        Boolean concluido,
        Instant criadoEm,
        long totalQuestoesRespondidas,
        long totalAcertos,
        double percentualAcerto
) {
}
