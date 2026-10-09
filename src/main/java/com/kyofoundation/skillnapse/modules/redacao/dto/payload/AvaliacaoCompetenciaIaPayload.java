package com.kyofoundation.skillnapse.modules.redacao.dto.payload;

import java.util.List;

public record AvaliacaoCompetenciaIaPayload(
        String nomeCompetencia,
        Double nota,
        String comentarios,
        List<String> desviosIdentificados
) {
}
