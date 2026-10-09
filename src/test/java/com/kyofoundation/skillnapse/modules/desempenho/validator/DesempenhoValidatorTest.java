package com.kyofoundation.skillnapse.modules.desempenho.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesempenhoValidatorTest {

    private DesempenhoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DesempenhoValidator();
    }

    @Test
    @DisplayName("Deve validar propriedade do plano com sucesso")
    void deveValidarPropriedadePlanoComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario).build();

        assertThatCode(() -> validator.validarPropriedadePlano(usuario, plano))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando plano pertencer a outro usuario")
    void deveLancarForbiddenQuandoPlanoPertencerAOutroUsuario() {
        Usuario usuario1 = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario usuario2 = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario2).build();

        assertThatThrownBy(() -> validator.validarPropriedadePlano(usuario1, plano))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para acessar os dados analíticos deste plano de estudo.");
    }

    @Test
    @DisplayName("Deve validar limite valido com sucesso")
    void deveValidarLimiteValido() {
        assertThatCode(() -> validator.validarLimiteTopicosCriticos(5))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validarLimiteTopicosCriticos(1))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validarLimiteTopicosCriticos(50))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando limite for menor que 1 ou maior que 50")
    void deveLancarBadRequestQuandoLimiteInvalido() {
        assertThatThrownBy(() -> validator.validarLimiteTopicosCriticos(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O limite de tópicos críticos deve ser entre 1 e 50.");

        assertThatThrownBy(() -> validator.validarLimiteTopicosCriticos(-5))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O limite de tópicos críticos deve ser entre 1 e 50.");

        assertThatThrownBy(() -> validator.validarLimiteTopicosCriticos(51))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O limite de tópicos críticos deve ser entre 1 e 50.");
    }
}
