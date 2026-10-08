package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Bloco de horário pedagógico aprovado pelo estudante para persistência")
public record BlocoParaAplicarRequest(
        @NotNull(message = "O dia da semana é obrigatório.")
        @Schema(description = "Dia da semana alocado", example = "SEGUNDA")
        DiaSemana diaSemana,

        @NotNull(message = "O horário de início é obrigatório.")
        @Schema(description = "Horário de início", example = "19:00:00")
        LocalTime horaInicio,

        @NotNull(message = "O horário de término é obrigatório.")
        @Schema(description = "Horário de término", example = "20:00:00")
        LocalTime horaFim,

        @Schema(description = "Tipo do bloco pedagógico", example = "FOCO_TEORIA")
        TipoBloco tipoBloco,

        @Schema(description = "Identificador da matéria vinculada")
        UUID materiaId,

        @Schema(description = "Identificador do tópico vinculado")
        UUID topicoId,

        @Schema(description = "Justificativa pedagógica gerada pela IA")
        String justificativaPedagogica
) {
}
