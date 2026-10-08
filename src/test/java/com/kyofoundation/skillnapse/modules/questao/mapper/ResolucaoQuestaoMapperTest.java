package com.kyofoundation.skillnapse.modules.questao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.dto.response.HistoricoTentativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.ResultadoResolucaoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResolucaoQuestaoMapperTest {

    @Test
    @DisplayName("Deve converter parâmetros para entidade TentativaQuestao")
    void deveConverterParaEntity() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Questao questao = Questao.builder().id(UUID.randomUUID()).build();
        AlternativaQuestao alternativa = AlternativaQuestao.builder().id(UUID.randomUUID()).build();

        TentativaQuestao tentativa = ResolucaoQuestaoMapper.toEntity(
                usuario,
                questao,
                alternativa,
                true,
                60,
                null,
                null
        );

        assertThat(tentativa).isNotNull();
        assertThat(tentativa.getUsuario()).isEqualTo(usuario);
        assertThat(tentativa.getQuestao()).isEqualTo(questao);
        assertThat(tentativa.getAlternativaEscolhida()).isEqualTo(alternativa);
        assertThat(tentativa.getAcertou()).isTrue();
        assertThat(tentativa.getTempoGastoSegundos()).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve converter TentativaQuestao para ResultadoResolucaoResponse")
    void deveConverterParaResultadoResponse() {
        Questao questao = Questao.builder()
                .id(UUID.randomUUID())
                .explicacaoGabarito("Art. 5º da CF")
                .build();
        AlternativaQuestao altEscolhida = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .letra("A")
                .build();
        AlternativaQuestao altCorreta = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .letra("B")
                .build();

        TentativaQuestao tentativa = TentativaQuestao.builder()
                .id(UUID.randomUUID())
                .questao(questao)
                .alternativaEscolhida(altEscolhida)
                .acertou(false)
                .tempoGastoSegundos(45)
                .respondidoEm(Instant.now())
                .build();

        ResultadoResolucaoResponse response = ResolucaoQuestaoMapper.toResultadoResponse(tentativa, altCorreta);

        assertThat(response).isNotNull();
        assertThat(response.tentativaId()).isEqualTo(tentativa.getId());
        assertThat(response.questaoId()).isEqualTo(questao.getId());
        assertThat(response.alternativaEscolhidaId()).isEqualTo(altEscolhida.getId());
        assertThat(response.letraEscolhida()).isEqualTo("A");
        assertThat(response.acertou()).isFalse();
        assertThat(response.alternativaCorretaId()).isEqualTo(altCorreta.getId());
        assertThat(response.letraCorreta()).isEqualTo("B");
        assertThat(response.explicacaoGabarito()).isEqualTo("Art. 5º da CF");
    }

    @Test
    @DisplayName("Deve converter TentativaQuestao para HistoricoTentativaResponse")
    void deveConverterParaHistoricoResponse() {
        Questao questao = Questao.builder()
                .id(UUID.randomUUID())
                .enunciado("Enunciado da questão")
                .assuntoGeral("Civil")
                .topicoReferencia("Pessoas")
                .build();
        AlternativaQuestao altEscolhida = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .letra("C")
                .build();

        TentativaQuestao tentativa = TentativaQuestao.builder()
                .id(UUID.randomUUID())
                .questao(questao)
                .alternativaEscolhida(altEscolhida)
                .acertou(true)
                .tempoGastoSegundos(30)
                .respondidoEm(Instant.now())
                .build();

        HistoricoTentativaResponse response = ResolucaoQuestaoMapper.toHistoricoResponse(tentativa);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(tentativa.getId());
        assertThat(response.enunciadoQuestao()).isEqualTo("Enunciado da questão");
        assertThat(response.assuntoGeral()).isEqualTo("Civil");
        assertThat(response.topicoReferencia()).isEqualTo("Pessoas");
        assertThat(response.acertou()).isTrue();
    }
}
