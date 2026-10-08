package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CriarBlocoRevisaoTemplateRequest(
        @NotNull(message = "O ID do template semanal é obrigatório.")
        @Schema(description = "Identificador do template semanal", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID templateSemanalId,

        @NotNull(message = "O dia da semana é obrigatório.")
        @Schema(description = "Dia da semana do bloco", example = "SEGUNDA")
        DiaSemana diaSemana,

        @NotNull(message = "O horário de início é obrigatório.")
        @Schema(description = "Horário de início (formato HH:mm:ss ou HH:mm)", example = "19:00:00")
        LocalTime horaInicio,

        @NotNull(message = "O horário de término é obrigatório.")
        @Schema(description = "Horário de término (formato HH:mm:ss ou HH:mm)", example = "20:30:00")
        LocalTime horaFim,

        @Schema(description = "Identificador opcional da matéria associada")
        UUID materiaId,

        @NotEmpty(message = "A lista de tópicos para revisão não pode ser vazia.")
        @Schema(description = "Lista de identificadores dos tópicos a serem revisados no bloco")
        List<UUID> topicoIds
) {
}
