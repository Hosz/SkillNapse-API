package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

public record EditarBlocoHorarioRequest(
        @Schema(description = "Novo dia da semana", example = "TERCA")
        DiaSemana diaSemana,

        @Schema(description = "Novo horário de início", example = "10:00:00")
        LocalTime horaInicio,

        @Schema(description = "Novo horário de término", example = "11:30:00")
        LocalTime horaFim,

        @Schema(description = "Novo tipo de bloco", example = "REVISAO")
        TipoBloco tipoBloco,

        @Schema(description = "Novo identificador da matéria associada")
        UUID materiaId,

        @Schema(description = "Novo identificador do tópico associado")
        UUID topicoId
) {
}
