package com.kyofoundation.skillnapse.modules.gamificacao.dto.response;

import java.util.UUID;

public record MetaDiariaResponse(
        UUID id,
        UUID usuarioId,
        int metaMinutosEstudo,
        int metaQuestoesResolvidas
) {
}
