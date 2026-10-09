package com.kyofoundation.skillnapse.modules.questao.mapper;

import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.AlternativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoDetalheResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoResumoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuestaoMapperTest {

    @Test
    @DisplayName("Deve converter CriarQuestaoRequest para Questao entity com alternativas")
    void deveConverterParaEntity() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Português",
                "Crase",
                "Assinale a frase correta quanto ao uso da crase.",
                "Fui à cidade exige crase.",
                DificuldadeQuestao.MEDIA,
                "FCC",
                2023,
                List.of(
                        new CriarAlternativaRequest("a", "Fui à cidade", true),
                        new CriarAlternativaRequest("b", "Fui a pé", false)
                )
        );

        Questao questao = QuestaoMapper.toEntity(request, "hashSHA256");

        assertThat(questao).isNotNull();
        assertThat(questao.getAssuntoGeral()).isEqualTo("Português");
        assertThat(questao.getTopicoReferencia()).isEqualTo("Crase");
        assertThat(questao.getHashEnunciado()).isEqualTo("hashSHA256");
        assertThat(questao.getDificuldade()).isEqualTo(DificuldadeQuestao.MEDIA);
        assertThat(questao.getAlternativas()).hasSize(2);
        assertThat(questao.getAlternativas().getFirst().getLetra()).isEqualTo("A");
        assertThat(questao.getAlternativas().getFirst().getCorreta()).isTrue();
    }

    @Test
    @DisplayName("Deve converter Questao para QuestaoResumoResponse")
    void deveConverterParaResumoResponse() {
        Questao questao = Questao.builder()
                .id(UUID.randomUUID())
                .assuntoGeral("Direito Administrativo")
                .topicoReferencia("Atos Administrativos")
                .enunciado("O ato administrativo prescinde de motivação?")
                .dificuldade(DificuldadeQuestao.DIFICIL)
                .banca("CESPE")
                .ano(2022)
                .criadoEm(Instant.now())
                .alternativas(new ArrayList<>())
                .build();

        QuestaoResumoResponse response = QuestaoMapper.toResumoResponse(questao);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(questao.getId());
        assertThat(response.assuntoGeral()).isEqualTo("Direito Administrativo");
        assertThat(response.totalAlternativas()).isZero();
    }

    @Test
    @DisplayName("Deve converter Questao para QuestaoDetalheResponse com alternativas")
    void deveConverterParaDetalheResponse() {
        Questao questao = Questao.builder()
                .id(UUID.randomUUID())
                .assuntoGeral("Informática")
                .topicoReferencia("Redes")
                .enunciado("Protocolo HTTP opera na camada de aplicação?")
                .explicacaoGabarito("Sim, HTTP é camada de aplicação.")
                .dificuldade(DificuldadeQuestao.FACIL)
                .geradaPorIa(false)
                .criadoEm(Instant.now())
                .alternativas(new ArrayList<>())
                .build();

        AlternativaQuestao alt = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .questao(questao)
                .letra("A")
                .texto("Verdadeiro")
                .correta(true)
                .build();
        questao.getAlternativas().add(alt);

        QuestaoDetalheResponse response = QuestaoMapper.toDetalheResponse(questao);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(questao.getId());
        assertThat(response.alternativas()).hasSize(1);
        AlternativaResponse altResp = response.alternativas().getFirst();
        assertThat(altResp.letra()).isEqualTo("A");
        assertThat(altResp.correta()).isTrue();
    }
}
