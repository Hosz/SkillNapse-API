package com.kyofoundation.skillnapse.modules.desempenho.dto.response;

import com.kyofoundation.skillnapse.modules.desempenho.enums.NivelCriticidadeTopico;

import java.util.UUID;

public record TopicoCriticoResponse(
        UUID topicoId,
        String tituloTopico,
        UUID materiaId,
        String materiaNome,
        Integer pesoEdital,
        Double taxaAcerto,
        Long totalTentativas,
        Double indiceSeveridade,
        NivelCriticidadeTopico criticidade
) {
}
