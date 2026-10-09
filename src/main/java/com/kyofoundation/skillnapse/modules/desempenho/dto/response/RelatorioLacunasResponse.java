package com.kyofoundation.skillnapse.modules.desempenho.dto.response;

import java.util.List;
import java.util.UUID;

public record RelatorioLacunasResponse(
        UUID planoId,
        String planoTitulo,
        Double scoreProntidao,
        Long totalQuestoesRespondidas,
        Long totalAcertos,
        Double taxaAcertoGeral,
        Integer totalTopicosCriticos,
        List<ResumoMateriaDesempenhoResponse> materias
) {
}
