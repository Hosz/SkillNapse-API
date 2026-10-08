package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoSugeridoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.AutoAgendamentoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.BlocoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AutoAgendamentoMapperTest {

    @Test
    @DisplayName("Deve mapear bloco estruturado da IA para BlocoSugeridoResponse com dados enriquecidos")
    void deveMapearBlocoComSucesso() {
        UUID matId = UUID.randomUUID();
        UUID topId = UUID.randomUUID();

        Materia materia = Materia.builder().id(matId).nome("Direito Constitucional").build();
        Topico topico = Topico.builder().id(topId).titulo("Direitos Fundamentais").build();

        BlocoIaEstruturado blocoIa = new BlocoIaEstruturado(
                DiaSemana.SEGUNDA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                TipoBloco.FOCO_TEORIA,
                matId,
                topId,
                "Prioridade alta no edital"
        );

        BlocoSugeridoResponse response = AutoAgendamentoMapper.toBlocoSugerido(
                blocoIa, Map.of(matId, materia), Map.of(topId, topico)
        );

        assertThat(response).isNotNull();
        assertThat(response.diaSemana()).isEqualTo(DiaSemana.SEGUNDA);
        assertThat(response.materiaNome()).isEqualTo("Direito Constitucional");
        assertThat(response.topicoTitulo()).isEqualTo("Direitos Fundamentais");
        assertThat(response.justificativaPedagogica()).isEqualTo("Prioridade alta no edital");
    }

    @Test
    @DisplayName("Deve calcular horas e total de blocos ao converter para SugestaoAutoAgendamentoResponse")
    void deveConverterParaSugestaoResponse() {
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).titulo("Concurso Polícia Federal").build();

        BlocoIaEstruturado b1 = new BlocoIaEstruturado(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 0), TipoBloco.FOCO_TEORIA, null, null, "Teoria"
        );
        BlocoIaEstruturado b2 = new BlocoIaEstruturado(
                DiaSemana.TERCA, LocalTime.of(19, 0), LocalTime.of(21, 0), TipoBloco.SIMULADO, null, null, "Simulado 2h"
        );

        AutoAgendamentoIaEstruturado iaResult = new AutoAgendamentoIaEstruturado(
                "Distribuição balanceada", List.of(b1, b2)
        );

        SugestaoAutoAgendamentoResponse sugestao = AutoAgendamentoMapper.toSugestaoResponse(
                plano, iaResult, Map.of(), Map.of()
        );

        assertThat(sugestao.planoEstudoId()).isEqualTo(plano.getId());
        assertThat(sugestao.totalBlocosSugeridos()).isEqualTo(2);
        assertThat(sugestao.totalHorasSemanais()).isEqualTo(3.0); // 1h + 2h
        assertThat(sugestao.resumoPedagogico()).isEqualTo("Distribuição balanceada");
    }

    @Test
    @DisplayName("Deve converter corretamente para ResultadoAutoAgendamentoResponse")
    void deveConverterParaResultadoResponse() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).nome("Template Teste").build();
        BlocoSugeridoResponse b1 = new BlocoSugeridoResponse(
                DiaSemana.SEXTA, LocalTime.of(14, 0), LocalTime.of(15, 0), TipoBloco.REVISAO, null, null, null, null, "Revisão"
        );

        ResultadoAutoAgendamentoResponse resultado = AutoAgendamentoMapper.toResultadoResponse(
                template, 1, "Resumo", List.of(b1)
        );

        assertThat(resultado.templateSemanalId()).isEqualTo(template.getId());
        assertThat(resultado.nomeTemplate()).isEqualTo("Template Teste");
        assertThat(resultado.totalBlocosPersistidos()).isEqualTo(1);
        assertThat(resultado.blocosPersistidos()).hasSize(1);
    }
}
