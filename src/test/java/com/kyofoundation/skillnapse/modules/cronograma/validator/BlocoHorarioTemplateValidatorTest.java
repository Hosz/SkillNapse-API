package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlocoHorarioTemplateValidatorTest {

    @Mock
    private BlocoHorarioTemplateRepository repository;

    private BlocoHorarioTemplateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BlocoHorarioTemplateValidator(repository);
    }

    @Test
    @DisplayName("Deve validar criacao de bloco de horario com sucesso")
    void deveValidarCriacaoComSucesso() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        CriarBlocoHorarioRequest request = new CriarBlocoHorarioRequest(
                DiaSemana.SEGUNDA,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                TipoBloco.FOCO_TEORIA,
                null,
                null
        );

        when(repository.existeSobreposicaoHorario(eq(template), eq(DiaSemana.SEGUNDA), any(), any())).thenReturn(false);

        assertThatCode(() -> validator.validarCriacao(request, template)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando horario de termino for anterior ou igual ao inicio")
    void deveLancarExcecaoHorarioInvertido() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        CriarBlocoHorarioRequest request = new CriarBlocoHorarioRequest(
                DiaSemana.SEGUNDA,
                LocalTime.of(15, 0),
                LocalTime.of(14, 0),
                TipoBloco.FOCO_TEORIA,
                null,
                null
        );

        assertThatThrownBy(() -> validator.validarCriacao(request, template))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O horário de término deve ser posterior ao horário de início.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando houver sobreposicao de horarios na criacao")
    void deveLancarExcecaoSobreposicaoCriacao() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        CriarBlocoHorarioRequest request = new CriarBlocoHorarioRequest(
                DiaSemana.TERCA,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                TipoBloco.FOCO_TEORIA,
                null,
                null
        );

        when(repository.existeSobreposicaoHorario(eq(template), eq(DiaSemana.TERCA), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> validator.validarCriacao(request, template))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Já existe um bloco de horário conflitante para TERCA no intervalo informado.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando houver sobreposicao de horarios na edicao")
    void deveLancarExcecaoSobreposicaoEdicao() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        BlocoHorarioTemplate blocoExistente = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .diaSemana(DiaSemana.QUARTA)
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(10, 0))
                .build();

        EditarBlocoHorarioRequest request = new EditarBlocoHorarioRequest(
                DiaSemana.QUARTA,
                LocalTime.of(9, 30),
                LocalTime.of(11, 0),
                null,
                null,
                null
        );

        when(repository.existeSobreposicaoHorarioDesconsiderandoBloco(
                eq(template), eq(DiaSemana.QUARTA), eq(blocoExistente.getId()), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> validator.validarEdicao(request, blocoExistente, template))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Já existe um bloco de horário conflitante para QUARTA no intervalo informado.");
    }

    @Test
    @DisplayName("Deve validar se bloco pertence ao template semanal")
    void deveValidarPertencimentoAoTemplate() {
        UUID templateId = UUID.randomUUID();
        TemplateSemanal template = TemplateSemanal.builder().id(templateId).build();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder().id(UUID.randomUUID()).templateSemanal(template).build();

        assertThatCode(() -> validator.validarBlocoPertenceTemplate(bloco, template)).doesNotThrowAnyException();

        TemplateSemanal outroTemplate = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarBlocoPertenceTemplate(bloco, outroTemplate))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Bloco de horário não pertence ao template semanal informado.");
    }

    @Test
    @DisplayName("Deve validar materia e topico pertencentes ao usuario e consistentes entre si")
    void deveValidarMateriaETopico() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(usuarioId).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).materia(materia).build();

        assertThatCode(() -> validator.validarMateriaETopico(usuario, materia, topico)).doesNotThrowAnyException();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarMateriaETopico(outroUsuario, materia, topico))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("A matéria indicada não pertence ao usuário.");
    }
}
