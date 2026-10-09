package com.kyofoundation.skillnapse.modules.redacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.AvaliacaoCompetenciaIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissaoRedacaoValidatorTest {

    private final SubmissaoRedacaoValidator validator = new SubmissaoRedacaoValidator();

    @Test
    @DisplayName("validarRequisicao deve lançar BadRequestException quando request for nulo ou campos inválidos")
    void deveLancarQuandoRequestInvalido() {
        assertThatThrownBy(() -> validator.validarRequisicao(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não podem ser nulos");

        assertThatThrownBy(() -> validator.validarRequisicao(new SubmeterRedacaoRequest(null, "texto")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("O ID do tema de redação é obrigatório.");

        assertThatThrownBy(() -> validator.validarRequisicao(new SubmeterRedacaoRequest(UUID.randomUUID(), "   ")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pode estar vazio");
    }

    @Test
    @DisplayName("validarRequisicao deve lançar BadRequestException quando texto for menor que 300 ou maior que 5000")
    void deveLancarQuandoTextoForaDosLimites() {
        UUID temaId = UUID.randomUUID();
        String textoCurto = "a".repeat(299);
        String textoLongo = "a".repeat(5001);

        assertThatThrownBy(() -> validator.validarRequisicao(new SubmeterRedacaoRequest(temaId, textoCurto)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("entre 300 e 5000 caracteres");

        assertThatThrownBy(() -> validator.validarRequisicao(new SubmeterRedacaoRequest(temaId, textoLongo)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("entre 300 e 5000 caracteres");
    }

    @Test
    @DisplayName("validarRequisicao deve passar com sucesso quando dados forem válidos")
    void devePassarValidacaoRequestValido() {
        UUID temaId = UUID.randomUUID();
        String textoValido = "a".repeat(350);

        assertThatCode(() -> validator.validarRequisicao(new SubmeterRedacaoRequest(temaId, textoValido)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarPropriedadeTema deve lançar ForbiddenException se tema pertencer a plano de outro usuário")
    void deveLancarSeTemaDeOutroUsuario() {
        Usuario usuario1 = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario usuario2 = Usuario.builder().id(UUID.randomUUID()).build();

        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(usuario2).build();
        TemaRedacao tema = TemaRedacao.builder().id(UUID.randomUUID()).planoEstudo(plano).build();

        assertThatThrownBy(() -> validator.validarPropriedadeTema(usuario1, tema))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Você não possui permissão para submeter redação para este tema.");
    }

    @Test
    @DisplayName("validarPropriedadeSubmissao deve lançar ForbiddenException se submissão pertencer a outro usuário")
    void deveLancarSeSubmissaoDeOutroUsuario() {
        Usuario usuario1 = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario usuario2 = Usuario.builder().id(UUID.randomUUID()).build();

        SubmissaoRedacao submissao = SubmissaoRedacao.builder().id(UUID.randomUUID()).usuario(usuario2).build();

        assertThatThrownBy(() -> validator.validarPropriedadeSubmissao(usuario1, submissao))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Você não possui permissão para acessar esta submissão de redação.");
    }

    @Test
    @DisplayName("validarFeedbackIa deve lançar BadRequestException para notas ou competências inválidas")
    void deveLancarSeFeedbackIaInvalido() {
        assertThatThrownBy(() -> validator.validarFeedbackIa(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Não foi possível obter a correção analítica");

        FeedbackCorrecaoIaPayload notaInvalida = new FeedbackCorrecaoIaPayload(
                1001.0, List.of(), "Parecer", List.of()
        );
        assertThatThrownBy(() -> validator.validarFeedbackIa(notaInvalida))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("compreendida entre 0.0 e 1000.0");

        FeedbackCorrecaoIaPayload semCompetencias = new FeedbackCorrecaoIaPayload(
                800.0, Collections.emptyList(), "Parecer", List.of()
        );
        assertThatThrownBy(() -> validator.validarFeedbackIa(semCompetencias))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deve conter a avaliação das competências");

        FeedbackCorrecaoIaPayload compNotaNegativa = new FeedbackCorrecaoIaPayload(
                800.0,
                List.of(new AvaliacaoCompetenciaIaPayload("Gramática", -1.0, "Comentário", List.of())),
                "Parecer",
                List.of()
        );
        assertThatThrownBy(() -> validator.validarFeedbackIa(compNotaNegativa))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nota de cada competência deve estar compreendida entre 0.0 e 1000.0");
    }

    @Test
    @DisplayName("validarFeedbackIa deve passar para feedback analítico íntegro")
    void devePassarFeedbackValido() {
        FeedbackCorrecaoIaPayload valido = new FeedbackCorrecaoIaPayload(
                920.0,
                List.of(new AvaliacaoCompetenciaIaPayload("Gramática", 180.0, "Excelente", List.of())),
                "Parecer analítico completo.",
                List.of()
        );
        assertThatCode(() -> validator.validarFeedbackIa(valido)).doesNotThrowAnyException();
    }
}
