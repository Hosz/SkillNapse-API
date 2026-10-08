package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ConteudoBlocoRevisaoResponse(
        UUID blocoId,
        TipoOrigemBloco origem,
        LocalDate dataReferencia,
        LocalTime horaInicio,
        LocalTime horaFim,
        UUID materiaId,
        String materiaNome,
        int totalTopicos,
        List<TopicoRevisaoItemResponse> topicos,
        long totalCardsCadastrados,
        int totalCardsParaRevisar,
        List<FlashcardRevisaoItemResponse> cardsParaRevisar
) {
}
