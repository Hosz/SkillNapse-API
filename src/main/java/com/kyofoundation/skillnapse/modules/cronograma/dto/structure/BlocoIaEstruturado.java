package com.kyofoundation.skillnapse.modules.cronograma.dto.structure;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;

import java.time.LocalTime;
import java.util.UUID;

public record BlocoIaEstruturado(
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        TipoBloco tipoBloco,
        UUID materiaId,
        UUID topicoId,
        String justificativaPedagogica
) {
}
