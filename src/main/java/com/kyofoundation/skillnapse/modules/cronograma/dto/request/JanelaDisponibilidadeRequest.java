package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Schema(description = "Janela de disponibilidade horária do estudante em um dia específico")
public record JanelaDisponibilidadeRequest(
        @NotNull(message = "O dia da semana é obrigatório.")
        @Schema(description = "Dia da semana", example = "SEGUNDA")
        DiaSemana diaSemana,

        @NotNull(message = "O horário de início é obrigatório.")
        @Schema(description = "Horário de início da janela", example = "19:00:00")
        LocalTime horaInicio,

        @NotNull(message = "O horário de término é obrigatório.")
        @Schema(description = "Horário de término da janela", example = "22:00:00")
        LocalTime horaFim
) {
}
