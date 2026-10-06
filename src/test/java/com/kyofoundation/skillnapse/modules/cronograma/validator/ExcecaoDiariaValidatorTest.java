package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.RegistrarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcecaoDiariaValidatorTest {

    @Mock
    private ExcecaoDiariaRepository excecaoDiariaRepository;

    private ExcecaoDiariaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ExcecaoDiariaValidator(excecaoDiariaRepository);
    }

    @Test
    @DisplayName("Deve validar cancelamento de bloco com sucesso")
    void deveValidarCancelamentoComSucesso() {
        LocalDate data = LocalDate.of(2026, 10, 15); // Quinta-feira
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        BlocoHorarioTemplate blocoOrigem = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .diaSemana(DiaSemana.QUINTA)
                .build();

        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.CANCELAR_BLOCO,
                blocoOrigem.getId(),
                null,
                null,
                null,
                null,
                null
        );

        when(excecaoDiariaRepository.existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigem(usuario, data, blocoOrigem)).thenReturn(false);

        assertThatCode(() -> validator.validarCriacao(request, data, usuario, blocoOrigem)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando cancelamento nao informar bloco de origem")
    void deveLancarExcecaoCancelamentoSemBloco() {
        LocalDate data = LocalDate.of(2026, 10, 15);
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.CANCELAR_BLOCO,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> validator.validarCriacao(request, data, usuario, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O bloco do template de origem é obrigatório para a ação CANCELAR_BLOCO.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando bloco de origem for de outro dia da semana")
    void deveLancarExcecaoDiaSemanaIncompativel() {
        LocalDate data = LocalDate.of(2026, 10, 15); // Quinta-feira
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        BlocoHorarioTemplate blocoOrigem = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .diaSemana(DiaSemana.SEGUNDA) // Diferente de QUINTA
                .build();

        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.CANCELAR_BLOCO,
                blocoOrigem.getId(),
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> validator.validarCriacao(request, data, usuario, blocoOrigem))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O bloco de origem (SEGUNDA) não pertence ao dia da semana desta data (QUINTA).");
    }

    @Test
    @DisplayName("Deve lancar ConflictException quando ja existir excecao para o bloco na data")
    void deveLancarConflitoQuandoExcecaoDuplicada() {
        LocalDate data = LocalDate.of(2026, 10, 15);
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        BlocoHorarioTemplate blocoOrigem = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .diaSemana(DiaSemana.QUINTA)
                .build();

        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.CANCELAR_BLOCO,
                blocoOrigem.getId(),
                null,
                null,
                null,
                null,
                null
        );

        when(excecaoDiariaRepository.existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigem(usuario, data, blocoOrigem)).thenReturn(true);

        assertThatThrownBy(() -> validator.validarCriacao(request, data, usuario, blocoOrigem))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe uma exceção registrada para este bloco nesta data.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando bloco avulso tiver horario invertido")
    void deveLancarExcecaoHorarioInvertidoBlocoAvulso() {
        LocalDate data = LocalDate.of(2026, 10, 15);
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.BLOCO_AVULSO,
                null,
                LocalTime.of(16, 0),
                LocalTime.of(15, 0),
                TipoBloco.FOCO_TEORIA,
                null,
                null
        );

        assertThatThrownBy(() -> validator.validarCriacao(request, data, usuario, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O horário de término deve ser posterior ao horário de início.");
    }

    @Test
    @DisplayName("Deve validar propriedade da excecao diaria")
    void deveValidarPropriedadeExcecao() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(usuarioId).build();
        ExcecaoDiaria excecao = ExcecaoDiaria.builder().id(UUID.randomUUID()).usuario(usuario).build();

        assertThatCode(() -> validator.validarPropriedade(usuario, excecao)).doesNotThrowAnyException();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarPropriedade(outroUsuario, excecao))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("A exceção diária não pertence ao usuário autenticado.");
    }
}
