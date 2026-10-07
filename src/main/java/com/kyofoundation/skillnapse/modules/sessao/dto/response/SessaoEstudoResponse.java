package com.kyofoundation.skillnapse.modules.sessao.dto.response;

import com.kyofoundation.skillnapse.modules.sessao.enums.StatusSessaoEstudo;

import java.time.Instant;
import java.util.UUID;

public record SessaoEstudoResponse(
        UUID id,
        UUID topicoId,
        String topicoTitulo,
        UUID materiaId,
        String materiaNome,
        Instant iniciadoEm,
        Instant finalizadoEm,
        Integer duracaoLiquidaSegundos,
        StatusSessaoEstudo status,
        String observacoes
) {
}
