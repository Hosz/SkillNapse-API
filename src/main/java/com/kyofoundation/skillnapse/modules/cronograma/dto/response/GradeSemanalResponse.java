package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GradeSemanalResponse(
        UUID templateId,
        String templateNome,
        Boolean ativo,
        Map<DiaSemana, List<BlocoHorarioResponse>> grade
) {
}
