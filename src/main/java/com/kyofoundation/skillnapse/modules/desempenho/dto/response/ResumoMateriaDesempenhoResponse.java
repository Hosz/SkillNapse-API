package com.kyofoundation.skillnapse.modules.desempenho.dto.response;

import java.util.List;
import java.util.UUID;

public record ResumoMateriaDesempenhoResponse(
        UUID materiaId,
        String materiaNome,
        String corHex,
        Long totalQuestoes,
        Long totalAcertos,
        Double taxaAcerto,
        Integer totalTopicosCriticos,
        List<TopicoLacunaResponse> topicos
) {
}
