package com.kyofoundation.skillnapse.modules.redacao.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubmissaoRedacaoResumoResponse(
        UUID id,
        UUID temaId,
        String temaTitulo,
        BigDecimal notaGeral,
        Instant corrigidoEm,
        Instant criadoEm
) {
}
