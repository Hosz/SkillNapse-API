package com.kyofoundation.skillnapse.modules.cronograma.support;

import com.kyofoundation.skillnapse.modules.cronograma.dto.response.AgendaDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoAgendaDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjecaoCronogramaSupportTest {

    private ProjecaoCronogramaSupport support;

    @BeforeEach
    void setUp() {
        support = new ProjecaoCronogramaSupport();
    }

    @Test
    @DisplayName("Deve projetar agenda do dia mesclando blocos normais, cancelados, substituidos e avulsos")
    void deveProjetarAgendaComSucesso() {
        LocalDate data = LocalDate.of(2026, 10, 15); // Quinta-feira
        TemplateSemanal template = TemplateSemanal.builder()
                .id(UUID.randomUUID())
                .nome("Rotina Padrão")
                .ativo(true)
                .build();

        // Bloco 1: 08:00 as 09:00 (permanece inalterado)
        BlocoHorarioTemplate bloco1 = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.QUINTA)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(9, 0))
                .tipoBloco(TipoBloco.FOCO_TEORIA)
                .build();

        // Bloco 2: 10:00 as 11:30 (sera cancelado por excecao)
        BlocoHorarioTemplate bloco2 = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.QUINTA)
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 30))
                .tipoBloco(TipoBloco.FOCO_TEORIA)
                .build();

        // Bloco 3: 14:00 as 15:30 (sera substituido por excecao para 15:00 as 16:30)
        BlocoHorarioTemplate bloco3 = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.QUINTA)
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(15, 30))
                .tipoBloco(TipoBloco.FOCO_TEORIA)
                .build();

        ExcecaoDiaria excecaoCancelar = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .dataExcecao(data)
                .blocoTemplateOrigem(bloco2)
                .tipoAcao(TipoAcaoExcecao.CANCELAR_BLOCO)
                .build();

        ExcecaoDiaria excecaoSubstituir = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .dataExcecao(data)
                .blocoTemplateOrigem(bloco3)
                .tipoAcao(TipoAcaoExcecao.SUBSTITUIR_HORARIO)
                .horaInicio(LocalTime.of(15, 0))
                .horaFim(LocalTime.of(16, 30))
                .tipoBloco(TipoBloco.REVISAO)
                .build();

        ExcecaoDiaria excecaoAvulso = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .dataExcecao(data)
                .tipoAcao(TipoAcaoExcecao.BLOCO_AVULSO)
                .horaInicio(LocalTime.of(19, 0))
                .horaFim(LocalTime.of(20, 0))
                .tipoBloco(TipoBloco.SIMULADO)
                .build();

        List<BlocoHorarioTemplate> blocosTemplate = List.of(bloco1, bloco2, bloco3);
        List<ExcecaoDiaria> excecoes = List.of(excecaoCancelar, excecaoSubstituir, excecaoAvulso);

        AgendaDiariaResponse agenda = support.projetarAgendaDoDia(data, template, blocosTemplate, excecoes);

        assertThat(agenda.data()).isEqualTo(data);
        assertThat(agenda.diaSemana()).isEqualTo(DiaSemana.QUINTA);
        assertThat(agenda.templateOrigemId()).isEqualTo(template.getId());

        // Blocos finais esperados: bloco1 (08:00), bloco3 substituido (15:00), bloco avulso (19:00). Bloco 2 foi cancelado.
        List<BlocoAgendaDiariaResponse> blocos = agenda.blocos();
        assertThat(blocos).hasSize(3);

        // Bloco 1: 08:00 (TEMPLATE)
        assertThat(blocos.get(0).horaInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(blocos.get(0).origem()).isEqualTo(TipoOrigemBloco.TEMPLATE);

        // Bloco 2 projetado: 15:00 (EXCECAO substituida)
        assertThat(blocos.get(1).horaInicio()).isEqualTo(LocalTime.of(15, 0));
        assertThat(blocos.get(1).origem()).isEqualTo(TipoOrigemBloco.EXCECAO);
        assertThat(blocos.get(1).tipoAcaoExcecao()).isEqualTo(TipoAcaoExcecao.SUBSTITUIR_HORARIO);

        // Bloco 3 projetado: 19:00 (EXCECAO avulsa)
        assertThat(blocos.get(2).horaInicio()).isEqualTo(LocalTime.of(19, 0));
        assertThat(blocos.get(2).origem()).isEqualTo(TipoOrigemBloco.EXCECAO);
        assertThat(blocos.get(2).tipoAcaoExcecao()).isEqualTo(TipoAcaoExcecao.BLOCO_AVULSO);
    }
}
