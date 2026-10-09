package com.kyofoundation.skillnapse.modules.desempenho.dto.response;

import com.kyofoundation.skillnapse.modules.desempenho.enums.NivelCriticidadeTopico;
import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;

import java.util.UUID;

public record TopicoLacunaResponse(
        UUID topicoId,
        String titulo,
        Integer pesoEdital,
        NivelProficiencia nivelProficiencia,
        Long totalTentativas,
        Long totalAcertos,
        Double taxaAcerto,
        Double tempoMedioSegundos,
        NivelCriticidadeTopico criticidade,
        Boolean concluido
) {
}
