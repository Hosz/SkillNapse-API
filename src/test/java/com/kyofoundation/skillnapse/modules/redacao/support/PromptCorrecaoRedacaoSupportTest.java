package com.kyofoundation.skillnapse.modules.redacao.support;

import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptCorrecaoRedacaoSupportTest {

    private final PromptCorrecaoRedacaoSupport support = new PromptCorrecaoRedacaoSupport();

    @Test
    @DisplayName("Deve construir prompt de correção com dados completos do tema")
    void deveConstruirPromptComDadosCompletos() {
        TemaRedacao tema = TemaRedacao.builder()
                .id(UUID.randomUUID())
                .titulo("A Inteligência Artificial e a Gestão Pública")
                .textosMotivadores("Texto I: Governança digital. Texto II: Eficiência administrativa.")
                .criteriosAvaliacao("Extensão de 30 linhas abordando riscos éticos e ganhos de produtividade.")
                .build();

        String textoAluno = "A modernização da administração pública através de sistemas inteligentes é um imperativo contemporâneo...";

        String prompt = support.construirPromptCorrecao(tema, textoAluno);

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("A Inteligência Artificial e a Gestão Pública");
        assertThat(prompt).contains("Governança digital");
        assertThat(prompt).contains("Extensão de 30 linhas");
        assertThat(prompt).contains(textoAluno);
        assertThat(prompt).contains("Gramática e Norma-Padrão");
        assertThat(prompt).contains("Coesão e Coerência");
        assertThat(prompt).contains("Estrutura Dissertativa e Argumentação");
        assertThat(prompt).contains("Atendimento ao Tema e Itens da Proposta");
        assertThat(prompt).contains("JSON");
    }

    @Test
    @DisplayName("Deve utilizar fallback de critérios quando critérios do tema forem nulos")
    void deveConstruirPromptComCriteriosFallback() {
        TemaRedacao tema = TemaRedacao.builder()
                .id(UUID.randomUUID())
                .titulo("Desafios da Mobilidade Urbana")
                .textosMotivadores("Texto motivador sobre trânsito e sustentabilidade.")
                .criteriosAvaliacao(null)
                .build();

        String textoAluno = "O desenvolvimento sustentável dos centros urbanos exige investimento maciço em transporte coletivo...";

        String prompt = support.construirPromptCorrecao(tema, textoAluno);

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Desafios da Mobilidade Urbana");
        assertThat(prompt).contains("Padrão dissertativo-argumentativo formal");
    }
}
