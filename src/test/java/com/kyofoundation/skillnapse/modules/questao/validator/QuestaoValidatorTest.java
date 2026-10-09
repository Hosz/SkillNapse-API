package com.kyofoundation.skillnapse.modules.questao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.finder.QuestaoFinder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestaoValidatorTest {

    @Mock
    private QuestaoFinder questaoFinder;

    @InjectMocks
    private QuestaoValidator questaoValidator;

    @Test
    @DisplayName("Deve validar criação com sucesso para questão válida")
    void deveValidarCriacaoComSucesso() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Direito Constitucional",
                "Direitos Fundamentais",
                "São direitos sociais a educação, a saúde e o trabalho?",
                "Art. 6º da CF/88.",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "Sim", true),
                        new CriarAlternativaRequest("B", "Não", false)
                )
        );

        when(questaoFinder.existsByHashEnunciado("hash123")).thenReturn(false);

        assertThatCode(() -> questaoValidator.validarCriacao(request, "hash123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException para request nulo")
    void deveLancarBadRequestExceptionParaRequestNulo() {
        assertThatThrownBy(() -> questaoValidator.validarCriacao(null, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados da questão não podem ser nulos.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException para menos de 2 alternativas")
    void deveLancarBadRequestParaMenosDeDuasAlternativas() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(new CriarAlternativaRequest("A", "Texto", true))
        );

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A questão deve conter entre 2 e 5 alternativas.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException para mais de 5 alternativas")
    void deveLancarBadRequestParaMaisDeCincoAlternativas() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "1", true),
                        new CriarAlternativaRequest("B", "2", false),
                        new CriarAlternativaRequest("C", "3", false),
                        new CriarAlternativaRequest("D", "4", false),
                        new CriarAlternativaRequest("E", "5", false),
                        new CriarAlternativaRequest("F", "6", false)
                )
        );

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A questão deve conter entre 2 e 5 alternativas.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando não há alternativa correta")
    void deveLancarBadRequestSemAlternativaCorreta() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "1", false),
                        new CriarAlternativaRequest("B", "2", false)
                )
        );

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A questão deve conter exatamente uma alternativa correta.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando há mais de uma alternativa correta")
    void deveLancarBadRequestComMaisDeUmaAlternativaCorreta() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "1", true),
                        new CriarAlternativaRequest("B", "2", true)
                )
        );

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A questão deve conter exatamente uma alternativa correta.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando existem letras de alternativas duplicadas")
    void deveLancarBadRequestComLetrasDuplicadas() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "1", true),
                        new CriarAlternativaRequest("a", "2", false)
                )
        );

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hash123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("As alternativas não podem possuir letras repetidas.");
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando o hash do enunciado já existe")
    void deveLancarConflictExceptionQuandoHashDuplicado() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Dir Const",
                "Topico",
                "Enunciado",
                "Gabarito",
                DificuldadeQuestao.FACIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "1", true),
                        new CriarAlternativaRequest("B", "2", false)
                )
        );

        when(questaoFinder.existsByHashEnunciado("hashDuplicado")).thenReturn(true);

        assertThatThrownBy(() -> questaoValidator.validarCriacao(request, "hashDuplicado"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Questão com o mesmo enunciado já cadastrada no acervo.");
    }
}
