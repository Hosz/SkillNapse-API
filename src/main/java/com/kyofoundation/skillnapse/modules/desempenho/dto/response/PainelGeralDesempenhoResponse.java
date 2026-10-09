package com.kyofoundation.skillnapse.modules.desempenho.dto.response;

import java.util.List;

public record PainelGeralDesempenhoResponse(
        Long totalQuestoesRespondidas,
        Long totalAcertos,
        Double taxaAcertoGeral,
        Double tempoMedioGeralSegundos,
        Integer totalTopicosCriticos,
        List<TopicoCriticoResponse> principaisLacunas,
        List<ResumoMateriaDesempenhoResponse> materias
) {
}
