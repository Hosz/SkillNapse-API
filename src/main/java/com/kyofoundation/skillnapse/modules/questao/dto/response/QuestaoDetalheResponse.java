package com.kyofoundation.skillnapse.modules.questao.dto.response;

import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuestaoDetalheResponse(
        UUID id,
        String assuntoGeral,
        String topicoReferencia,
        String enunciado,
        String explicacaoGabarito,
        DificuldadeQuestao dificuldade,
        String banca,
        Integer ano,
        Boolean geradaPorIa,
        Instant criadoEm,
        List<AlternativaResponse> alternativas
) {
}
