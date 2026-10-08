package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CriarBlocoRevisaoDiarioRequest(
        @NotNull(message = "A data da exceção diária é obrigatória.")
        @Schema(description = "Data do bloco avulso de revisão", example = "2026-10-15")
        LocalDate dataExcecao,

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
