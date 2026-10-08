package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FlashcardRevisaoItemResponse(
        UUID id,
        UUID baralhoId,
        String baralhoTitulo,
        UUID topicoId,
        String topicoTitulo,
        String frente,
        String verso,
        BigDecimal fatorFacilidade,
        int intervaloDias,
        int repeticoes,
        LocalDate proximaRevisao
) {
}
