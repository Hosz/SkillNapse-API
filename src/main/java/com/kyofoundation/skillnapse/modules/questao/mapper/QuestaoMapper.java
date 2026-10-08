package com.kyofoundation.skillnapse.modules.questao.mapper;

import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.AlternativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoDetalheResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoResumoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class QuestaoMapper {

    public static Questao toEntity(CriarQuestaoRequest request, String hashEnunciado) {
        if (request == null) {
            return null;
        }

        Questao questao = Questao.builder()
                .assuntoGeral(request.assuntoGeral().trim())
                .topicoReferencia(request.topicoReferencia().trim())
                .enunciado(request.enunciado().trim())
                .hashEnunciado(hashEnunciado)
                .explicacaoGabarito(request.explicacaoGabarito().trim())
                .dificuldade(request.dificuldade())
                .banca(request.banca() != null ? request.banca().trim() : null)
                .ano(request.ano())
                .geradaPorIa(false)
                .alternativas(new ArrayList<>())
                .build();

        if (request.alternativas() != null) {
            for (CriarAlternativaRequest altReq : request.alternativas()) {
                AlternativaQuestao alt = toAlternativaEntity(altReq, questao);
                questao.getAlternativas().add(alt);
            }
        }

        return questao;
    }

    public static AlternativaQuestao toAlternativaEntity(CriarAlternativaRequest request, Questao questao) {
        if (request == null) {
            return null;
        }

        return AlternativaQuestao.builder()
                .questao(questao)
                .letra(request.letra().trim().toUpperCase())
                .texto(request.texto().trim())
                .correta(Boolean.TRUE.equals(request.correta()))
                .build();
    }

    public static AlternativaResponse toAlternativaResponse(AlternativaQuestao alt) {
        if (alt == null) {
            return null;
        }

        return new AlternativaResponse(
                alt.getId(),
                alt.getLetra(),
                alt.getTexto(),
                alt.getCorreta()
        );
    }

    public static QuestaoResumoResponse toResumoResponse(Questao questao) {
        if (questao == null) {
            return null;
        }

        int totalAlternativas = questao.getAlternativas() != null ? questao.getAlternativas().size() : 0;

        return new QuestaoResumoResponse(
                questao.getId(),
                questao.getAssuntoGeral(),
                questao.getTopicoReferencia(),
                questao.getEnunciado(),
                questao.getDificuldade(),
                questao.getBanca(),
                questao.getAno(),
                questao.getCriadoEm(),
                totalAlternativas
        );
    }

    public static QuestaoDetalheResponse toDetalheResponse(Questao questao) {
        if (questao == null) {
            return null;
        }

        List<AlternativaResponse> alternativas = questao.getAlternativas() != null
                ? questao.getAlternativas().stream().map(QuestaoMapper::toAlternativaResponse).toList()
                : Collections.emptyList();

        return new QuestaoDetalheResponse(
                questao.getId(),
                questao.getAssuntoGeral(),
                questao.getTopicoReferencia(),
                questao.getEnunciado(),
                questao.getExplicacaoGabarito(),
                questao.getDificuldade(),
                questao.getBanca(),
                questao.getAno(),
                questao.getGeradaPorIa(),
                questao.getCriadoEm(),
                alternativas
        );
    }
}
