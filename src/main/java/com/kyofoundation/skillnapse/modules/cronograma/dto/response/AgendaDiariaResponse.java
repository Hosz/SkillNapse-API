package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AgendaDiariaResponse(
        LocalDate data,
        DiaSemana diaSemana,
        UUID templateOrigemId,
        String templateOrigemNome,
        List<BlocoAgendaDiariaResponse> blocos
) {
}
