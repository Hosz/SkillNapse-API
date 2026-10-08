package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Bloco de horário sugerido pela inteligência artificial")
public record BlocoSugeridoResponse(
        @Schema(description = "Dia da semana alocado")
        DiaSemana diaSemana,

        @Schema(description = "Horário de início")
        LocalTime horaInicio,

        @Schema(description = "Horário de término")
        LocalTime horaFim,

        @Schema(description = "Tipo do bloco pedagógico")
        TipoBloco tipoBloco,

        @Schema(description = "ID da matéria vinculada (quando aplicável)")
        UUID materiaId,

        @Schema(description = "Nome da matéria vinculada")
        String materiaNome,

        @Schema(description = "ID do tópico vinculado (quando aplicável)")
        UUID topicoId,

        @Schema(description = "Título do tópico vinculado")
        String topicoTitulo,

        @Schema(description = "Justificativa pedagógica da IA para este bloco")
        String justificativaPedagogica
) {
}
