package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BlocoRevisaoResponse(
        UUID id,
        TipoOrigemBloco origem,
        UUID templateSemanalId,
        DiaSemana diaSemana,
        LocalDate dataExcecao,
        LocalTime horaInicio,
        LocalTime horaFim,
        TipoBloco tipoBloco,
        UUID materiaId,
        String materiaNome,
        List<TopicoRevisaoItemResponse> topicosRevisao
) {
}
