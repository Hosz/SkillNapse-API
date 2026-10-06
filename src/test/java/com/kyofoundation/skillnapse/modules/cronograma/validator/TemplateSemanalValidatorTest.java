package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateSemanalValidatorTest {

    private TemplateSemanalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateSemanalValidator();
    }

    @Test
    @DisplayName("Deve validar criacao de template com sucesso")
    void deveValidarCriacaoComSucesso() {
        CriarTemplateSemanalRequest request = new CriarTemplateSemanalRequest("Rotina Concurso", true);
        assertThatCode(() -> validator.validarCriacao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando request de criacao for nulo ou nome em branco")
    void deveLancarExcecaoCriacaoInvalida() {
        assertThatThrownBy(() -> validator.validarCriacao(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados do template semanal não podem ser nulos.");

        assertThatThrownBy(() -> validator.validarCriacao(new CriarTemplateSemanalRequest("   ", true)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O nome do template semanal não pode estar em branco.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando nome ultrapassar 100 caracteres")
    void deveLancarExcecaoNomeMuitoLongo() {
        String nomeLongo = "a".repeat(101);
        assertThatThrownBy(() -> validator.validarCriacao(new CriarTemplateSemanalRequest(nomeLongo, true)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O nome do template não pode ter mais de 100 caracteres.");
    }

    @Test
    @DisplayName("Deve validar edicao com sucesso")
    void deveValidarEdicaoComSucesso() {
        EditarTemplateSemanalRequest request = new EditarTemplateSemanalRequest("Novo Nome", false);
        assertThatCode(() -> validator.validarEdicao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando edicao nao contiver campos")
    void deveLancarExcecaoEdicaoSemCampos() {
        assertThatThrownBy(() -> validator.validarEdicao(new EditarTemplateSemanalRequest(null, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Pelo menos um campo deve ser informado para atualização do template.");
    }

    @Test
    @DisplayName("Deve validar se template pertence ao usuario")
    void deveValidarPropriedadeDoTemplate() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(usuarioId).build();
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).usuario(usuario).build();

        assertThatCode(() -> validator.validarTemplatePertenceUsuario(usuario, template)).doesNotThrowAnyException();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarTemplatePertenceUsuario(outroUsuario, template))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("O template semanal não pertence ao usuário autenticado.");
    }
}
