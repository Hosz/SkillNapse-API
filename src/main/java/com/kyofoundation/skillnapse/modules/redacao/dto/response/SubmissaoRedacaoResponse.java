package com.kyofoundation.skillnapse.modules.redacao.dto.response;

import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubmissaoRedacaoResponse(
        UUID id,
        UUID temaId,
        String temaTitulo,
        String textoAluno,
        BigDecimal notaGeral,
        FeedbackCorrecaoIaPayload feedback,
        Instant corrigidoEm,
        Instant criadoEm
) {
}
