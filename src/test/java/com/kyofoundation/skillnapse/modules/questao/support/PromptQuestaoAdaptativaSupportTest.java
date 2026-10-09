package com.kyofoundation.skillnapse.modules.questao.support;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptQuestaoAdaptativaSupportTest {

    private final PromptQuestaoAdaptativaSupport support = new PromptQuestaoAdaptativaSupport();

    @Test
    @DisplayName("Deve construir prompt com tópico, matéria e banca especificada")
    void deveConstruirPromptComBancaEspecificada() {
        Materia materia = Materia.builder()
                .id(UUID.randomUUID())
                .nome("Direito Constitucional")
                .build();

        Topico topico = Topico.builder()
                .id(UUID.randomUUID())
                .titulo("Direitos Fundamentais - Artigo 5º")
                .materia(materia)
                .build();

        String prompt = support.construirPromptGeracao(topico, 5, "CESPE/Cebraspe");

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Direitos Fundamentais - Artigo 5º");
        assertThat(prompt).contains("Direito Constitucional");
        assertThat(prompt).contains("CESPE/Cebraspe");
        assertThat(prompt).contains("5 questão(ões)");
        assertThat(prompt).contains("JSON");
    }

    @Test
    @DisplayName("Deve construir prompt com estilo padrão quando banca for nula ou em branco")
    void deveConstruirPromptComBancaPadrao() {
        Topico topico = Topico.builder()
                .id(UUID.randomUUID())
                .titulo("Cálculo Diferencial")
                .build();

        String prompt = support.construirPromptGeracao(topico, 3, "   ");

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Cálculo Diferencial");
        assertThat(prompt).contains("Bancas Tradicionais de Concurso");
        assertThat(prompt).contains("3 questão(ões)");
    }
}
