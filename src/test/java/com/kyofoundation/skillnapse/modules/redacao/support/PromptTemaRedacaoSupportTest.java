package com.kyofoundation.skillnapse.modules.redacao.support;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemaRedacaoSupportTest {

    private final PromptTemaRedacaoSupport support = new PromptTemaRedacaoSupport();

    @Test
    @DisplayName("Deve construir prompt com todos os parâmetros fornecidos")
    void deveConstruirPromptComParametrosCompletos() {
        PlanoEstudo plano = PlanoEstudo.builder()
                .id(UUID.randomUUID())
                .titulo("Concurso TCU Auditor")
                .build();

        Materia materia = Materia.builder()
                .id(UUID.randomUUID())
                .nome("Direito Administrativo")
                .build();

        Topico topico = Topico.builder()
                .id(UUID.randomUUID())
                .titulo("Atos Administrativos e Discricionariedade")
                .build();

        String prompt = support.construirPromptGeracao(
                plano,
                materia,
                topico,
                "Cebraspe",
                "Estudo de Caso"
        );

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Concurso TCU Auditor");
        assertThat(prompt).contains("Direito Administrativo");
        assertThat(prompt).contains("Atos Administrativos e Discricionariedade");
        assertThat(prompt).contains("Cebraspe");
        assertThat(prompt).contains("Estudo de Caso");
        assertThat(prompt).contains("JSON");
    }

    @Test
    @DisplayName("Deve construir prompt com fallbacks quando parâmetros opcionais forem nulos ou vazios")
    void deveConstruirPromptComFallbacks() {
        PlanoEstudo plano = PlanoEstudo.builder()
                .id(UUID.randomUUID())
                .titulo("Receita Federal")
                .build();

        String prompt = support.construirPromptGeracao(
                plano,
                null,
                null,
                "   ",
                null
        );

        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Receita Federal");
        assertThat(prompt).contains("Geral / Conhecimentos Interdisciplinares");
        assertThat(prompt).contains("Temas Contemporâneos e Relevantes ao Cargo");
        assertThat(prompt).contains("Bancas Tradicionais de Concurso Público");
        assertThat(prompt).contains("Dissertativo-Argumentativo");
    }
}
