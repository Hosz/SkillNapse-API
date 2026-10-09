package com.kyofoundation.skillnapse.modules.questao.dto.payload;

import java.util.List;

public record LoteQuestoesIaPayload(
        List<QuestaoGeradaIaPayload> questoes
) {
}
