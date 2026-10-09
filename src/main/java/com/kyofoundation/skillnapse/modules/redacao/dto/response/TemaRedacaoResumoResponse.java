package com.kyofoundation.skillnapse.modules.redacao.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TemaRedacaoResumoResponse(
        UUID id,
        UUID planoEstudoId,
        String titulo,
        Boolean geradoPorIa,
        Instant criadoEm,
        Long totalSubmissoes
) {
}
