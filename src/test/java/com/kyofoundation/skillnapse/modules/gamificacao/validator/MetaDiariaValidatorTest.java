package com.kyofoundation.skillnapse.modules.gamificacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetaDiariaValidatorTest {

    private MetaDiariaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MetaDiariaValidator();
    }

    @Test
    @DisplayName("[validarAtualizacao] Deve validar com sucesso parâmetros válidos")
    void deveValidarAtualizacaoValida() {
        AtualizarMetaDiariaRequest request = new AtualizarMetaDiariaRequest(90, 20);
        assertThatCode(() -> validator.validarAtualizacao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("[validarAtualizacao] Deve lançar BadRequestException se request for nulo")
    void deveLancarBadRequestSeRequestNulo() {
        assertThatThrownBy(() -> validator.validarAtualizacao(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não podem ser nulos");
    }

    @Test
    @DisplayName("[validarAtualizacao] Deve lançar BadRequestException se minutos <= 0 ou exceder 1440")
    void deveLancarBadRequestSeMinutosInvalidos() {
        assertThatThrownBy(() -> validator.validarAtualizacao(new AtualizarMetaDiariaRequest(0, 10)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("superior a zero");

        assertThatThrownBy(() -> validator.validarAtualizacao(new AtualizarMetaDiariaRequest(-10, 10)))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> validator.validarAtualizacao(new AtualizarMetaDiariaRequest(1500, 10)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("1440 minutos");
    }

    @Test
    @DisplayName("[validarAtualizacao] Deve lançar BadRequestException se questoes < 0 ou exceder 1000")
    void deveLancarBadRequestSeQuestoesInvalidas() {
        assertThatThrownBy(() -> validator.validarAtualizacao(new AtualizarMetaDiariaRequest(60, -1)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pode ser negativa");

        assertThatThrownBy(() -> validator.validarAtualizacao(new AtualizarMetaDiariaRequest(60, 1005)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("1000");
    }
}
