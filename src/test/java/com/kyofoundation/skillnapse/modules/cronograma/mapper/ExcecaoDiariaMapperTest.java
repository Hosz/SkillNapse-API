package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.RegistrarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ExcecaoDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExcecaoDiariaMapperTest {

    @Test
    @DisplayName("Deve mapear criacao de excecao diaria de bloco avulso de forma null-safe")
    void deveMapearCriacaoBlocoAvulso() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        LocalDate data = LocalDate.of(2026, 10, 15);
        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.BLOCO_AVULSO,
                null,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                TipoBloco.SIMULADO,
                null,
                null
        );

        ExcecaoDiaria excecao = ExcecaoDiariaMapper.toCriarExcecao(usuario, null, data, request, null, null);

        assertThat(excecao.getUsuario()).isEqualTo(usuario);
        assertThat(excecao.getDataExcecao()).isEqualTo(data);
        assertThat(excecao.getTipoAcao()).isEqualTo(TipoAcaoExcecao.BLOCO_AVULSO);
        assertThat(excecao.getBlocoTemplateOrigem()).isNull();
        assertThat(excecao.getHoraInicio()).isEqualTo(LocalTime.of(18, 0));
        assertThat(excecao.getHoraFim()).isEqualTo(LocalTime.of(19, 0));
        assertThat(excecao.getTipoBloco()).isEqualTo(TipoBloco.SIMULADO);
    }

    @Test
    @DisplayName("Deve mapear para response de forma null-safe sem lançar NPE")
    void deveMapearParaResponseNullSafe() {
        ExcecaoDiaria excecao = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .dataExcecao(LocalDate.of(2026, 10, 15))
                .tipoAcao(TipoAcaoExcecao.BLOCO_AVULSO)
                .blocoTemplateOrigem(null)
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 0))
                .tipoBloco(TipoBloco.FOCO_TEORIA)
                .materia(null)
                .topico(null)
                .build();

        ExcecaoDiariaResponse response = ExcecaoDiariaMapper.toResponse(excecao);

        assertThat(response.id()).isEqualTo(excecao.getId());
        assertThat(response.tipoAcao()).isEqualTo(TipoAcaoExcecao.BLOCO_AVULSO);
        assertThat(response.blocoTemplateOrigemId()).isNull();
        assertThat(response.blocoTemplateOrigemDiaSemana()).isNull();
        assertThat(response.materiaId()).isNull();
        assertThat(response.topicoId()).isNull();
    }

    @Test
    @DisplayName("Deve mapear edicao parcial de excecao diaria")
    void deveMapearEdicaoParcial() {
        ExcecaoDiaria excecao = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .tipoAcao(TipoAcaoExcecao.SUBSTITUIR_HORARIO)
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(15, 0))
                .build();

        EditarExcecaoDiariaRequest request = new EditarExcecaoDiariaRequest(
                null,
                null,
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                TipoBloco.REVISAO,
                null,
                null
        );

        ExcecaoDiariaMapper.toEditarExcecao(request, excecao, null, null, null);

        assertThat(excecao.getHoraInicio()).isEqualTo(LocalTime.of(15, 0));
        assertThat(excecao.getHoraFim()).isEqualTo(LocalTime.of(16, 30));
        assertThat(excecao.getTipoBloco()).isEqualTo(TipoBloco.REVISAO);
    }
}
