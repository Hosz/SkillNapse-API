package com.kyofoundation.skillnapse.modules.edital.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.enums.StatusRascunhoEdital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RascunhoEditalValidatorTest {

    private RascunhoEditalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RascunhoEditalValidator();
    }

    @Test
    @DisplayName("Deve validar propriedade do rascunho com sucesso para o proprietario")
    void deveValidarPropriedadeComSucesso() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        RascunhoEdital rascunho = RascunhoEdital.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();

        assertThatCode(() -> validator.validarPropriedade(usuario, rascunho)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException se rascunho pertencer a outro usuario")
    void deveLancarForbiddenExceptionParaOutroUsuario() {
        Usuario dono = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario invasor = Usuario.builder().id(UUID.randomUUID()).build();
        RascunhoEdital rascunho = RascunhoEdital.builder()
                .id(UUID.randomUUID())
                .usuario(dono)
                .build();

        assertThatThrownBy(() -> validator.validarPropriedade(invasor, rascunho))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("não possui permissão");
    }

    @Test
    @DisplayName("Deve permitir edicao apenas em status AGUARDANDO_APROVACAO")
    void deveValidarStatusParaEdicao() {
        RascunhoEdital pendente = RascunhoEdital.builder().status(StatusRascunhoEdital.AGUARDANDO_APROVACAO).build();
        assertThatCode(() -> validator.validarStatusParaEdicao(pendente)).doesNotThrowAnyException();

        RascunhoEdital convertido = RascunhoEdital.builder().status(StatusRascunhoEdital.CONVERTIDO).build();
        assertThatThrownBy(() -> validator.validarStatusParaEdicao(convertido))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("já foi convertido");

        RascunhoEdital falha = RascunhoEdital.builder().status(StatusRascunhoEdital.FALHA).build();
        assertThatThrownBy(() -> validator.validarStatusParaEdicao(falha))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Apenas rascunhos com status AGUARDANDO_APROVACAO");
    }

    @Test
    @DisplayName("Deve permitir conversao apenas em status AGUARDANDO_APROVACAO")
    void deveValidarStatusParaConversao() {
        RascunhoEdital pendente = RascunhoEdital.builder().status(StatusRascunhoEdital.AGUARDANDO_APROVACAO).build();
        assertThatCode(() -> validator.validarStatusParaConversao(pendente)).doesNotThrowAnyException();

        RascunhoEdital convertido = RascunhoEdital.builder().status(StatusRascunhoEdital.CONVERTIDO).build();
        assertThatThrownBy(() -> validator.validarStatusParaConversao(convertido))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("já foi convertido");
    }
}
