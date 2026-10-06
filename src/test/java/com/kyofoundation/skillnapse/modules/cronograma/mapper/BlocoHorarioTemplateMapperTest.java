package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoHorarioResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlocoHorarioTemplateMapperTest {

    @Test
    @DisplayName("Deve mapear criacao de bloco de horario com campos nulos e default de tipo")
    void deveMapearCriacaoCorretamente() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        CriarBlocoHorarioRequest request = new CriarBlocoHorarioRequest(
                DiaSemana.QUINTA,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                null,
                null,
                null
        );

        BlocoHorarioTemplate bloco = BlocoHorarioTemplateMapper.toCriarBlocoHorario(template, request, null, null);

        assertThat(bloco.getDiaSemana()).isEqualTo(DiaSemana.QUINTA);
        assertThat(bloco.getHoraInicio()).isEqualTo(LocalTime.of(14, 0));
        assertThat(bloco.getHoraFim()).isEqualTo(LocalTime.of(15, 30));
        assertThat(bloco.getTipoBloco()).isEqualTo(TipoBloco.FOCO_TEORIA);
        assertThat(bloco.getMateria()).isNull();
        assertThat(bloco.getTopico()).isNull();
    }

    @Test
    @DisplayName("Deve mapear para response de forma null-safe sem lançar NPE")
    void deveMapearParaResponseNullSafe() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEXTA)
                .horaInicio(LocalTime.of(16, 0))
                .horaFim(LocalTime.of(17, 0))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(null)
                .topico(null)
                .build();

        BlocoHorarioResponse response = BlocoHorarioTemplateMapper.toResponse(bloco);

        assertThat(response.id()).isEqualTo(bloco.getId());
        assertThat(response.materiaId()).isNull();
        assertThat(response.materiaNome()).isNull();
        assertThat(response.topicoId()).isNull();
        assertThat(response.topicoTitulo()).isNull();
    }

    @Test
    @DisplayName("Deve mapear edicao parcial de bloco de horario")
    void deveMapearEdicaoParcial() {
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(9, 0))
                .tipoBloco(TipoBloco.FOCO_TEORIA)
                .build();

        EditarBlocoHorarioRequest request = new EditarBlocoHorarioRequest(
                DiaSemana.TERCA,
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                TipoBloco.SIMULADO,
                null,
                null
        );

        BlocoHorarioTemplateMapper.toEditarBlocoHorario(bloco, request, null, null);

        assertThat(bloco.getDiaSemana()).isEqualTo(DiaSemana.TERCA);
        assertThat(bloco.getHoraInicio()).isEqualTo(LocalTime.of(10, 0));
        assertThat(bloco.getHoraFim()).isEqualTo(LocalTime.of(11, 30));
        assertThat(bloco.getTipoBloco()).isEqualTo(TipoBloco.SIMULADO);
    }
}
