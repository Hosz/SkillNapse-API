package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;

import java.time.LocalTime;
import java.util.UUID;

public record BlocoHorarioResponse(
        UUID id,
        UUID templateSemanalId,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        TipoBloco tipoBloco,
        UUID materiaId,
        String materiaNome,
        UUID topicoId,
        String topicoTitulo
) {
}
