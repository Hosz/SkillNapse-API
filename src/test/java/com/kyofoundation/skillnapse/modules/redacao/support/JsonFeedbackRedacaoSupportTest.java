package com.kyofoundation.skillnapse.modules.redacao.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.AvaliacaoCompetenciaIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.SugestaoReescritaIaPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFeedbackRedacaoSupportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFeedbackRedacaoSupport support = new JsonFeedbackRedacaoSupport(objectMapper);

    @Test
    @DisplayName("Deve serializar e desserializar FeedbackCorrecaoIaPayload com fidelidade")
    void deveSerializarEDesserializarComSucesso() {
        FeedbackCorrecaoIaPayload feedback = new FeedbackCorrecaoIaPayload(
                8.75,
                List.of(new AvaliacaoCompetenciaIaPayload(
                        "Gramática", 9.0, "Excelente concordância", List.of("Desvio na linha 12")
                )),
                "Texto muito bem desenvolvido com repertório sólido.",
                List.of(new SugestaoReescritaIaPayload(
                        "Dessa forma se vê que", "Desse modo, constata-se que", "Maior formalidade acadêmica"
                ))
        );

        String json = support.serializar(feedback);
        assertThat(json).isNotBlank();
        assertThat(json).contains("Gramática");
        assertThat(json).contains("8.75");

        FeedbackCorrecaoIaPayload recuperado = support.desserializar(json);
        assertThat(recuperado).isNotNull();
        assertThat(recuperado.notaGeral()).isEqualTo(8.75);
        assertThat(recuperado.competencias()).hasSize(1);
        assertThat(recuperado.sugestoesReescrita()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar null quando entrada for nula ou em branco")
    void deveRetornarNullParaEntradasVazias() {
        assertThat(support.serializar(null)).isNull();
        assertThat(support.desserializar(null)).isNull();
        assertThat(support.desserializar("   ")).isNull();
    }
}
