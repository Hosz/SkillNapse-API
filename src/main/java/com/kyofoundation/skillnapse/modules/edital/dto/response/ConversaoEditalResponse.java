package com.kyofoundation.skillnapse.modules.edital.dto.response;

import java.util.UUID;

public record ConversaoEditalResponse(
        UUID rascunhoId,
        UUID planoEstudoId,
        String tituloPlano,
        Integer totalMateriasCriadas,
        Integer totalTopicosCriados
) {
}
