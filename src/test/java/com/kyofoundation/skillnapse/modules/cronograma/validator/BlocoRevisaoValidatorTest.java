package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlocoRevisaoValidatorTest {

    @Mock
    private BlocoHorarioTemplateRepository repository;

    private BlocoRevisaoValidator validator;

    private Usuario usuario;
    private TemplateSemanal template;

    @BeforeEach
    void setUp() {
        validator = new BlocoRevisaoValidator(repository);
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
        template = TemplateSemanal.builder().id(UUID.randomUUID()).usuario(usuario).build();
    }

    @Test
    @DisplayName("[validarCriacaoTemplate] Deve validar criacao com sucesso")
    void deveValidarCriacaoTemplateComSucesso() {
        UUID topicoId = UUID.randomUUID();
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                template.getId(),
                DiaSemana.TERCA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 30),
                null,
                List.of(topicoId)
        );

        when(repository.existeSobreposicaoHorario(eq(template), eq(DiaSemana.TERCA), any(), any())).thenReturn(false);

        assertThatCode(() -> validator.validarCriacaoTemplate(request, template, usuario)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[validarCriacaoTemplate] Deve lançar exceção se request nulo")
    void deveLancarExcecaoSeRequestTemplateNulo() {
        assertThatThrownBy(() -> validator.validarCriacaoTemplate(null, template, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não podem ser nulos");
    }

    @Test
    @DisplayName("[validarCriacaoTemplate] Deve lançar ForbiddenException se template de outro usuário")
    void deveLancarForbiddenSeTemplateDeOutroUsuario() {
        Usuario outro = Usuario.builder().id(UUID.randomUUID()).build();
        TemplateSemanal outroTemplate = TemplateSemanal.builder().id(UUID.randomUUID()).usuario(outro).build();
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                outroTemplate.getId(),
                DiaSemana.TERCA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                null,
                List.of(UUID.randomUUID())
        );

        assertThatThrownBy(() -> validator.validarCriacaoTemplate(request, outroTemplate, usuario))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("não pertence ao usuário autenticado");
    }

    @Test
    @DisplayName("[validarCriacaoTemplate] Deve lançar BadRequestException se horário término anterior ou igual ao início")
    void deveLancarBadRequestSeHorarioInvalidoTemplate() {
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                template.getId(),
                DiaSemana.TERCA,
                LocalTime.of(20, 0),
                LocalTime.of(19, 0),
                null,
                List.of(UUID.randomUUID())
        );

        assertThatThrownBy(() -> validator.validarCriacaoTemplate(request, template, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("[validarCriacaoTemplate] Deve lançar BadRequestException se houver sobreposição")
    void deveLancarBadRequestSeSobreposicaoTemplate() {
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                template.getId(),
                DiaSemana.TERCA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                null,
                List.of(UUID.randomUUID())
        );

        when(repository.existeSobreposicaoHorario(eq(template), eq(DiaSemana.TERCA), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> validator.validarCriacaoTemplate(request, template, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("conflitante");
    }

    @Test
    @DisplayName("[validarCriacaoDiaria] Deve validar criacao diária com sucesso")
    void deveValidarCriacaoDiariaComSucesso() {
        CriarBlocoRevisaoDiarioRequest request = new CriarBlocoRevisaoDiarioRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                null,
                List.of(UUID.randomUUID())
        );

        assertThatCode(() -> validator.validarCriacaoDiaria(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[validarCriacaoDiaria] Deve lançar exceção se dados nulos ou horários inconsistentes")
    void deveLancarExcecaoSeDadosDiariosInconsistentes() {
        assertThatThrownBy(() -> validator.validarCriacaoDiaria(null))
                .isInstanceOf(BadRequestException.class);

        CriarBlocoRevisaoDiarioRequest semData = new CriarBlocoRevisaoDiarioRequest(
                null, LocalTime.of(18, 0), LocalTime.of(19, 0), null, List.of(UUID.randomUUID())
        );
        assertThatThrownBy(() -> validator.validarCriacaoDiaria(semData))
                .isInstanceOf(BadRequestException.class);

        CriarBlocoRevisaoDiarioRequest horarioInvalido = new CriarBlocoRevisaoDiarioRequest(
                LocalDate.now(), LocalTime.of(19, 0), LocalTime.of(18, 0), null, List.of(UUID.randomUUID())
        );
        assertThatThrownBy(() -> validator.validarCriacaoDiaria(horarioInvalido))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("[validarTopicosRevisao] Deve validar tópicos com sucesso")
    void deveValidarTopicosRevisaoComSucesso() {
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();
        Topico topico1 = Topico.builder().id(UUID.randomUUID()).titulo("Tópico 1").materia(materia).build();
        Topico topico2 = Topico.builder().id(UUID.randomUUID()).titulo("Tópico 2").materia(materia).build();

        List<UUID> topicoIds = List.of(topico1.getId(), topico2.getId());

        assertThatCode(() -> validator.validarTopicosRevisao(usuario, materia, List.of(topico1, topico2), topicoIds))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[validarTopicosRevisao] Deve lançar BadRequestException se lista vazia ou divergência de contagem")
    void deveLancarBadRequestSeListaVaziaOuDivergencia() {
        assertThatThrownBy(() -> validator.validarTopicosRevisao(usuario, null, List.of(), List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ao menos um tópico");

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        assertThatThrownBy(() -> validator.validarTopicosRevisao(usuario, null, List.of(), List.of(id1, id2)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não foram encontrados");
    }

    @Test
    @DisplayName("[validarTopicosRevisao] Deve lançar ForbiddenException se tópico pertencer a outro usuário")
    void deveLancarForbiddenSeTopicoDeOutroUsuario() {
        Usuario outro = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo planoOutro = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(outro).build();
        Materia materiaOutro = Materia.builder().id(UUID.randomUUID()).planoEstudo(planoOutro).build();
        Topico topicoOutro = Topico.builder().id(UUID.randomUUID()).titulo("Topico Inválido").materia(materiaOutro).build();

        List<UUID> ids = List.of(topicoOutro.getId());

        assertThatThrownBy(() -> validator.validarTopicosRevisao(usuario, null, List.of(topicoOutro), ids))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("não pertencem ao usuário autenticado");
    }

    @Test
    @DisplayName("[validarTopicosRevisao] Deve lançar BadRequestException se tópico não pertencer à matéria do bloco")
    void deveLancarBadRequestSeTopicoNaoPertenceMateriaDoBloco() {
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario).build();
        Materia materiaA = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();
        Materia materiaB = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();

        Topico topicoB = Topico.builder().id(UUID.randomUUID()).titulo("Tópico de B").materia(materiaB).build();
        List<UUID> ids = List.of(topicoB.getId());

        assertThatThrownBy(() -> validator.validarTopicosRevisao(usuario, materiaA, List.of(topicoB), ids))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pertence à matéria");
    }

    @Test
    @DisplayName("[validarTipoBlocoRevisao] Deve lançar BadRequestException se bloco não for REVISAO")
    void deveLancarBadRequestSeNaoForRevisao() {
        assertThatCode(() -> validator.validarTipoBlocoRevisao(TipoBloco.REVISAO)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validarTipoBlocoRevisao(TipoBloco.FOCO_TEORIA))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("REVISAO");
    }

    @Test
    @DisplayName("[validarPropriedades] Deve validar propriedade de template e exceção")
    void deveValidarPropriedadeTemplateEExcecao() {
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder().id(UUID.randomUUID()).templateSemanal(template).build();
        ExcecaoDiaria excecao = ExcecaoDiaria.builder().id(UUID.randomUUID()).usuario(usuario).build();

        assertThatCode(() -> validator.validarPropriedadeTemplate(bloco, usuario)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarPropriedadeExcecao(excecao, usuario)).doesNotThrowAnyException();

        Usuario outro = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarPropriedadeTemplate(bloco, outro))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> validator.validarPropriedadeExcecao(excecao, outro))
                .isInstanceOf(ForbiddenException.class);
    }
}
