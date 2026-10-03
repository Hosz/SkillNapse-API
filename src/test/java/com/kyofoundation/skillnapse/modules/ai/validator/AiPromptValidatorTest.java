package com.kyofoundation.skillnapse.modules.ai.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiPromptValidatorTest {

    private AiPromptValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AiPromptValidator();
    }

    @Test
    @DisplayName("Deve validar com sucesso uma requisição válida")
    void deveValidarComSucessoRequisicaoValida() {
        PromptRequest request = new PromptRequest("Explique o princípio SRP", "Você é um mentor", 0.7, 500, Map.of());

        assertThatCode(() -> validator.validatePromptRequest(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando a requisição for nula")
    void deveLancarExcecaoQuandoRequisicaoNula() {
        assertThatThrownBy(() -> validator.validatePromptRequest(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A requisição de prompt não pode ser nula.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Deve lançar BadRequestException quando o prompt for vazio ou em branco")
    void deveLancarExcecaoQuandoPromptVazio(String blankPrompt) {
        PromptRequest request = new PromptRequest(blankPrompt);

        assertThatThrownBy(() -> validator.validatePromptRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O prompt não pode ser nulo ou vazio.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando o prompt for nulo")
    void deveLancarExcecaoQuandoPromptNulo() {
        PromptRequest request = new PromptRequest(null);

        assertThatThrownBy(() -> validator.validatePromptRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O prompt não pode ser nulo ou vazio.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando a temperatura for menor que zero")
    void deveLancarExcecaoQuandoTemperaturaNegativa() {
        PromptRequest request = new PromptRequest("Prompt válido", null, -0.1, 100, Map.of());

        assertThatThrownBy(() -> validator.validatePromptRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A temperatura deve estar entre 0.0 e 2.0.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando a temperatura for maior que 2.0")
    void deveLancarExcecaoQuandoTemperaturaAcimaDoLimite() {
        PromptRequest request = new PromptRequest("Prompt válido", null, 2.5, 100, Map.of());

        assertThatThrownBy(() -> validator.validatePromptRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A temperatura deve estar entre 0.0 e 2.0.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando maxTokens for menor ou igual a zero")
    void deveLancarExcecaoQuandoMaxTokensInvalido() {
        PromptRequest request = new PromptRequest("Prompt válido", null, 0.5, 0, Map.of());

        assertThatThrownBy(() -> validator.validatePromptRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A quantidade máxima de tokens deve ser maior que zero.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando responseType for nulo na requisição estruturada")
    void deveLancarExcecaoQuandoResponseTypeNulo() {
        PromptRequest request = new PromptRequest("Gere um JSON de matéria");

        assertThatThrownBy(() -> validator.validateStructuredRequest(request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O tipo de classe de resposta (responseType) não pode ser nulo.");
    }
}
