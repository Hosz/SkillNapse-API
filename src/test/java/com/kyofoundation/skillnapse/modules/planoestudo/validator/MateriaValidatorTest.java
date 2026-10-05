package com.kyofoundation.skillnapse.modules.planoestudo.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MateriaValidatorTest {

    private MateriaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MateriaValidator();
    }

    @Test
    @DisplayName("Deve validar criacao de materia com sucesso")
    void deveValidarCriacaoComSucesso() {
        CriarMateriaRequest request = new CriarMateriaRequest("Direito Constitucional", "#3B82F6", 1);
        assertThatCode(() -> validator.validarCriacao(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando cor hex for invalida")
    void deveLancarExcecaoQuandoCorHexInvalida() {
        CriarMateriaRequest requestCorInvalida = new CriarMateriaRequest("Materia", "azul", 1);
        CriarMateriaRequest requestHexSemHash = new CriarMateriaRequest("Materia", "3B82F6", 1);

        assertThatThrownBy(() -> validator.validarCriacao(requestCorInvalida))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A cor deve estar no formato hexadecimal #RRGGBB (ex: #3B82F6).");

        assertThatThrownBy(() -> validator.validarCriacao(requestHexSemHash))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A cor deve estar no formato hexadecimal #RRGGBB (ex: #3B82F6).");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando ordem for negativa")
    void deveLancarExcecaoQuandoOrdemNegativa() {
        CriarMateriaRequest request = new CriarMateriaRequest("Materia", "#FFFFFF", -1);

        assertThatThrownBy(() -> validator.validarCriacao(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A ordem da matéria não pode ser negativa.");
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando materia pertencer a outro usuario")
    void deveLancarForbiddenExceptionQuandoMateriaDeOutroUsuario() {
        Usuario usuario1 = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario usuario2 = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().usuario(usuario2).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();

        assertThatThrownBy(() -> validator.validarMateriaPertenceUsuario(usuario1, materia))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não tem permissão para acessar esta matéria.");
    }
}
