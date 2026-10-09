package com.kyofoundation.skillnapse.modules.redacao.dto.payload;

public record SugestaoReescritaIaPayload(
        String trechoOriginal,
        String trechoSugerido,
        String justificativa
) {
}
