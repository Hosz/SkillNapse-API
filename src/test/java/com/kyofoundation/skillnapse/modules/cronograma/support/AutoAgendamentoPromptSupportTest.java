package com.kyofoundation.skillnapse.modules.cronograma.support;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.JanelaDisponibilidadeRequest;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.EstrategiaAgendamento;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AutoAgendamentoPromptSupportTest {

    private AutoAgendamentoPromptSupport promptSupport;

    @BeforeEach
    void setUp() {
        promptSupport = new AutoAgendamentoPromptSupport();
    }

    @Test
    @DisplayName("Deve construir promptRequest formatando adequadamente materias, topicos e janelas")
    void deveConstruirPromptComSucesso() {
        PlanoEstudo plano = PlanoEstudo.builder()
                .id(UUID.randomUUID())
                .titulo("Plano Concurso Receita")
                .descricao("Preparação Auditor")
                .build();

        Materia materia = Materia.builder()
                .id(UUID.randomUUID())
                .nome("Direito Tributário")
                .build();

        Topico topico = Topico.builder()
                .id(UUID.randomUUID())
                .materia(materia)
                .titulo("Impostos Federais")
                .pesoEdital(5)
                .nivelProficiencia(NivelProficiencia.INICIANTE)
                .build();

        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(22, 0)
        );

        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                plano.getId(), List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        PromptRequest promptRequest = promptSupport.criarPrompt(plano, List.of(materia), List.of(topico), request);

        assertThat(promptRequest).isNotNull();
        assertThat(promptRequest.systemMessage()).contains("Interleaving");
        assertThat(promptRequest.prompt())
                .contains("Plano Concurso Receita")
                .contains("Direito Tributário")
                .contains("Impostos Federais")
                .contains("SEGUNDA: das 19:00 às 22:00")
                .contains("FOCO_PESO_EDITAL");
    }
}
