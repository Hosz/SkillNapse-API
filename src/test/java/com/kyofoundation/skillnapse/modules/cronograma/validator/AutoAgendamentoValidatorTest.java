package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.JanelaDisponibilidadeRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.EstrategiaAgendamento;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutoAgendamentoValidatorTest {

    private AutoAgendamentoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AutoAgendamentoValidator();
    }

    @Test
    @DisplayName("Deve validar requisicao valida com sucesso")
    void deveValidarRequisicaoValidaComSucesso() {
        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.SEGUNDA, LocalTime.of(18, 0), LocalTime.of(21, 0)
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                UUID.randomUUID(), List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        assertThatCode(() -> validator.validarRequisicao(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando requisicao for nula ou sem planoId")
    void deveFalharRequisicaoNulaOuSemPlano() {
        assertThatThrownBy(() -> validator.validarRequisicao(null))
                .isInstanceOf(BadRequestException.class);

        GerarAutoAgendamentoRequest semPlano = new GerarAutoAgendamentoRequest(
                null, List.of(new JanelaDisponibilidadeRequest(DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0))),
                60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );
        assertThatThrownBy(() -> validator.validarRequisicao(semPlano))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando disponibilidades forem vazias")
    void deveFalharDisponibilidadesVazias() {
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                UUID.randomUUID(), Collections.emptyList(), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        assertThatThrownBy(() -> validator.validarRequisicao(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando horaFim for menor ou igual a horaInicio")
    void deveFalharHoraFimInvalida() {
        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.TERCA, LocalTime.of(20, 0), LocalTime.of(19, 0)
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                UUID.randomUUID(), List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        assertThatThrownBy(() -> validator.validarRequisicao(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deve ser posterior");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando janela for menor que duracao do bloco")
    void deveFalharJanelaMenorQueBloco() {
        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.QUARTA, LocalTime.of(18, 0), LocalTime.of(18, 30) // 30 min
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                UUID.randomUUID(), List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL // bloco 60 min
        );

        assertThatThrownBy(() -> validator.validarRequisicao(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("menor que a duração de um bloco");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando houver sobreposicao interna de janelas no mesmo dia")
    void deveFalharSobreposicaoInternaDeJanelas() {
        JanelaDisponibilidadeRequest j1 = new JanelaDisponibilidadeRequest(
                DiaSemana.QUINTA, LocalTime.of(18, 0), LocalTime.of(20, 0)
        );
        JanelaDisponibilidadeRequest j2 = new JanelaDisponibilidadeRequest(
                DiaSemana.QUINTA, LocalTime.of(19, 0), LocalTime.of(21, 0)
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                UUID.randomUUID(), List.of(j1, j2), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        assertThatThrownBy(() -> validator.validarRequisicao(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sobrepostas para QUINTA");
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando plano pertencer a outro usuario")
    void deveFalharPlanoDeOutroUsuario() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario outro = Usuario.builder().id(UUID.randomUUID()).build();

        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(outro).titulo("Plano").build();
        List<Materia> materias = List.of(Materia.builder().id(UUID.randomUUID()).nome("Dir Const").build());

        assertThatThrownBy(() -> validator.validarPlano(plano, usuario, materias))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando plano nao tiver materias")
    void deveFalharPlanoSemMaterias() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario).titulo("Plano").build();

        assertThatThrownBy(() -> validator.validarPlano(plano, usuario, Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não possui matérias cadastradas");
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando template pertencer a outro usuario")
    void deveFalharTemplateDeOutroUsuario() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario outro = Usuario.builder().id(UUID.randomUUID()).build();

        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).usuario(outro).build();

        assertThatThrownBy(() -> validator.validarTemplate(template, usuario))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Deve validar aplicacao request com sucesso")
    void deveValidarAplicacaoRequestComSucesso() {
        com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest bloco =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest(
                        DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 0),
                        com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco.FOCO_TEORIA, null, null, "Estudo"
                );
        com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest request =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest(
                        UUID.randomUUID(), null, "Novo Template", true, "Resumo", List.of(bloco)
                );

        assertThatCode(() -> validator.validarAplicacaoRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar ao validar aplicacao request vazia ou sem plano")
    void deveFalharAplicacaoRequestInvalida() {
        assertThatThrownBy(() -> validator.validarAplicacaoRequest(null))
                .isInstanceOf(BadRequestException.class);

        com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest semPlano =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest(
                        null, null, "Nome", true, "Resumo", Collections.emptyList()
                );
        assertThatThrownBy(() -> validator.validarAplicacaoRequest(semPlano))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve falhar ao validar blocos com sobreposicao de horario no mesmo dia")
    void deveFalharBlocosComSobreposicao() {
        com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest b1 =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest(
                        DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 30),
                        com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco.FOCO_TEORIA, null, null, "B1"
                );
        com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest b2 =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest(
                        DiaSemana.SEGUNDA, LocalTime.of(20, 0), LocalTime.of(21, 0),
                        com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco.FOCO_TEORIA, null, null, "B2"
                );

        assertThatThrownBy(() -> validator.validarBlocosParaAplicacao(List.of(b1, b2), java.util.Map.of(), java.util.Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("conflita com");
    }
}
