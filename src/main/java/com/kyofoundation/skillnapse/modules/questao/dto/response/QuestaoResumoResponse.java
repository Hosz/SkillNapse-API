package com.kyofoundation.skillnapse.modules.questao.dto.response;

import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;

import java.time.Instant;
import java.util.UUID;

public record QuestaoResumoResponse(
        UUID id,
        String assuntoGeral,
        String topicoReferencia,
        String enunciado,
        DificuldadeQuestao dificuldade,
        String banca,
        Integer ano,
        Instant criadoEm,
        int totalAlternativas
) {
}
