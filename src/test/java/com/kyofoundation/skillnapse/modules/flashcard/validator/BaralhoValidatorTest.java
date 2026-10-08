package com.kyofoundation.skillnapse.modules.flashcard.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaralhoValidatorTest {

    private BaralhoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BaralhoValidator();
    }

    @Test
    @DisplayName("Deve validar CriarBaralhoRequest valido")
    void deveValidarCriacaoValida() {
        CriarBaralhoRequest request = new CriarBaralhoRequest("Título", "Descrição", null);
        assertThatCode(() -> validator.validarCriacao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se CriarBaralhoRequest for nulo ou sem titulo")
    void deveLancarExceptionCriacaoInvalida() {
        assertThatThrownBy(() -> validator.validarCriacao(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados de criação do baralho não podem ser nulos.");

        CriarBaralhoRequest semTitulo = new CriarBaralhoRequest("   ", "Descrição", null);
        assertThatThrownBy(() -> validator.validarCriacao(semTitulo))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O título do baralho é obrigatório.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se titulo do baralho ultrapassar 150 caracteres")
    void deveLancarExceptionTituloLongo() {
        String tituloLongo = "a".repeat(151);
        CriarBaralhoRequest request = new CriarBaralhoRequest(tituloLongo, null, null);

        assertThatThrownBy(() -> validator.validarCriacao(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O título do baralho não pode ultrapassar 150 caracteres.");
    }

    @Test
    @DisplayName("Deve validar AtualizarBaralhoRequest valido e invalido")
    void deveValidarAtualizacao() {
        AtualizarBaralhoRequest valido = new AtualizarBaralhoRequest("Novo Título", null, null);
        assertThatCode(() -> validator.validarAtualizacao(valido)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validarAtualizacao(null))
                .isInstanceOf(BadRequestException.class);

        AtualizarBaralhoRequest semTitulo = new AtualizarBaralhoRequest("", null, null);
        assertThatThrownBy(() -> validator.validarAtualizacao(semTitulo))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve validar propriedade do baralho com sucesso")
    void deveValidarPropriedadeBaralhoComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();

        assertThatCode(() -> validator.validarPropriedadeBaralho(usuario, baralho))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException se baralho pertencer a outro usuario")
    void deveLancarForbiddenSeBaralhoDeOutroUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        Baralho baralho = Baralho.builder().usuario(outroUsuario).build();

        assertThatThrownBy(() -> validator.validarPropriedadeBaralho(usuario, baralho))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para acessar ou modificar este baralho.");
    }

    @Test
    @DisplayName("Deve validar se materia pertence ao usuario")
    void deveValidarMateriaPertenceUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        PlanoEstudo plano = PlanoEstudo.builder().usuario(usuario).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();

        assertThatCode(() -> validator.validarMateriaPertenceUsuario(usuario, materia))
                .doesNotThrowAnyException();

        // Se materia for nula, é opcional e deve passar
        assertThatCode(() -> validator.validarMateriaPertenceUsuario(usuario, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException se materia pertencer a outro usuario")
    void deveLancarForbiddenSeMateriaDeOutroUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().usuario(outroUsuario).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();

        assertThatThrownBy(() -> validator.validarMateriaPertenceUsuario(usuario, materia))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para associar este baralho à matéria informada.");
    }
}
