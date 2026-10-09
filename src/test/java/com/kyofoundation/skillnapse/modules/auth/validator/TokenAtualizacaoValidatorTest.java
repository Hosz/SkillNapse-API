package com.kyofoundation.skillnapse.modules.auth.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenAtualizacaoValidatorTest {

    private TokenAtualizacaoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TokenAtualizacaoValidator();
    }

    @Test
    @DisplayName("Deve validar string de token valida sem lancar excecao")
    void deveValidarTokenStringComSucesso() {
        assertThatCode(() -> validator.validarTokenString("token-valido-123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando token for nulo ou em branco")
    void deveLancarBadRequestQuandoTokenNuloOuEmBranco() {
        assertThatThrownBy(() -> validator.validarTokenString(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O token de atualização não pode ser nulo ou vazio.");

        assertThatThrownBy(() -> validator.validarTokenString("   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O token de atualização não pode ser nulo ou vazio.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando token ultrapassar 255 caracteres")
    void deveLancarBadRequestQuandoTokenUltrapassar255Caracteres() {
        String tokenLongo = "a".repeat(256);
        assertThatThrownBy(() -> validator.validarTokenString(tokenLongo))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O token de atualização não pode ter mais de 255 caracteres.");
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedException quando token ja estiver revogado")
    void deveLancarUnauthorizedExceptionQuandoTokenRevogado() {
        TokenAtualizacao token = TokenAtualizacao.builder().revogado(true).build();

        assertThatThrownBy(() -> validator.validarNaoRevogado(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Tentativa de reutilização de token detectada. A sessão foi invalidada por segurança.");
    }

    @Test
    @DisplayName("Nao deve lancar excecao quando token nao estiver revogado")
    void naoDeveLancarExcecaoQuandoTokenNaoRevogado() {
        TokenAtualizacao token = TokenAtualizacao.builder().revogado(false).build();

        assertThatCode(() -> validator.validarNaoRevogado(token))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedException quando token estiver expirado")
    void deveLancarUnauthorizedExceptionQuandoTokenExpirado() {
        TokenAtualizacao token = TokenAtualizacao.builder()
                .expiraEm(Instant.now().minusSeconds(10))
                .build();

        assertThatThrownBy(() -> validator.validarNaoExpirado(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de atualização expirado.");
    }

    @Test
    @DisplayName("Nao deve lancar excecao quando token nao estiver expirado")
    void naoDeveLancarExcecaoQuandoTokenNaoExpirado() {
        TokenAtualizacao token = TokenAtualizacao.builder()
                .expiraEm(Instant.now().plusSeconds(3600))
                .build();

        assertThatCode(() -> validator.validarNaoExpirado(token))
                .doesNotThrowAnyException();
    }
}
