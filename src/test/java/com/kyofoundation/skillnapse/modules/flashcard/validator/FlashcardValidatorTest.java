package com.kyofoundation.skillnapse.modules.flashcard.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.RevisarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashcardValidatorTest {

    private FlashcardValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FlashcardValidator();
    }

    @Test
    @DisplayName("Deve validar CriarFlashcardRequest valido")
    void deveValidarCriacaoValida() {
        CriarFlashcardRequest request = new CriarFlashcardRequest(UUID.randomUUID(), null, "Frente", "Verso");
        assertThatCode(() -> validator.validarCriacao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException para CriarFlashcardRequest invalido")
    void deveLancarExceptionCriacaoInvalida() {
        assertThatThrownBy(() -> validator.validarCriacao(null))
                .isInstanceOf(BadRequestException.class);

        CriarFlashcardRequest semBaralho = new CriarFlashcardRequest(null, null, "F", "V");
        assertThatThrownBy(() -> validator.validarCriacao(semBaralho))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O ID do baralho é obrigatório.");

        CriarFlashcardRequest semFrente = new CriarFlashcardRequest(UUID.randomUUID(), null, "", "V");
        assertThatThrownBy(() -> validator.validarCriacao(semFrente))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O texto da frente do flashcard é obrigatório.");

        CriarFlashcardRequest semVerso = new CriarFlashcardRequest(UUID.randomUUID(), null, "F", "   ");
        assertThatThrownBy(() -> validator.validarCriacao(semVerso))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O texto do verso do flashcard é obrigatório.");
    }

    @Test
    @DisplayName("Deve validar AtualizarFlashcardRequest")
    void deveValidarAtualizacao() {
        AtualizarFlashcardRequest valido = new AtualizarFlashcardRequest(null, "Nova Frente", "Novo Verso");
        assertThatCode(() -> validator.validarAtualizacao(valido)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validarAtualizacao(null))
                .isInstanceOf(BadRequestException.class);

        AtualizarFlashcardRequest semFrente = new AtualizarFlashcardRequest(null, " ", "Novo Verso");
        assertThatThrownBy(() -> validator.validarAtualizacao(semFrente))
                .isInstanceOf(BadRequestException.class);

        AtualizarFlashcardRequest semVerso = new AtualizarFlashcardRequest(null, "Nova Frente", "");
        assertThatThrownBy(() -> validator.validarAtualizacao(semVerso))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve validar RevisarFlashcardRequest")
    void deveValidarRevisao() {
        RevisarFlashcardRequest valido = new RevisarFlashcardRequest(ClassificacaoResposta.BOM, 5);
        assertThatCode(() -> validator.validarRevisao(valido)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validarRevisao(null))
                .isInstanceOf(BadRequestException.class);

        RevisarFlashcardRequest semClassificacao = new RevisarFlashcardRequest(null, 5);
        assertThatThrownBy(() -> validator.validarRevisao(semClassificacao))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A classificação da resposta é obrigatória.");

        RevisarFlashcardRequest tempoNegativo = new RevisarFlashcardRequest(ClassificacaoResposta.BOM, -1);
        assertThatThrownBy(() -> validator.validarRevisao(tempoNegativo))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O tempo de resposta não pode ser negativo.");
    }

    @Test
    @DisplayName("Deve validar propriedade do flashcard atraves do baralho")
    void deveValidarPropriedadeFlashcard() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();
        Flashcard flashcard = Flashcard.builder().baralho(baralho).build();

        assertThatCode(() -> validator.validarPropriedadeFlashcard(usuario, flashcard))
                .doesNotThrowAnyException();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarPropriedadeFlashcard(outroUsuario, flashcard))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para acessar ou modificar este flashcard.");
    }

    @Test
    @DisplayName("Deve validar se topico pertence ao usuario")
    void deveValidarTopicoPertenceUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        PlanoEstudo plano = PlanoEstudo.builder().usuario(usuario).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();
        Topico topico = Topico.builder().materia(materia).build();

        assertThatCode(() -> validator.validarTopicoPertenceUsuario(usuario, topico))
                .doesNotThrowAnyException();

        // Topico opcional nulo passa
        assertThatCode(() -> validator.validarTopicoPertenceUsuario(usuario, null))
                .doesNotThrowAnyException();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        assertThatThrownBy(() -> validator.validarTopicoPertenceUsuario(outroUsuario, topico))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para associar este flashcard ao tópico informado.");
    }
}
