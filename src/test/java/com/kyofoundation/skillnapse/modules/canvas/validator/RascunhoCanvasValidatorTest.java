package com.kyofoundation.skillnapse.modules.canvas.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RascunhoCanvasValidatorTest {

    private RascunhoCanvasValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RascunhoCanvasValidator();
    }

    @Test
    @DisplayName("Deve validar request valido com sucesso")
    void deveValidarRequestValidoComSucesso() {
        UUID topicoId = UUID.randomUUID();
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, "{\"paths\":[]}");

        assertThatCode(() -> validator.validarSalvarRequest(request, topicoId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve permitir topicoId nulo na request se fornecido via topicoIdResolvido")
    void devePermitirTopicoIdNuloNaRequestSeResolvidoNaUrl() {
        UUID topicoId = UUID.randomUUID();
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(null, "{\"paths\":[]}");

        assertThatCode(() -> validator.validarSalvarRequest(request, topicoId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se request for nulo")
    void deveLancarExceptionSeRequestNulo() {
        assertThatThrownBy(() -> validator.validarSalvarRequest(null, UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados do rascunho de canvas não podem ser nulos.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se topicoIdResolvido for nulo")
    void deveLancarExceptionSeTopicoIdResolvidoNulo() {
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(null, "{\"paths\":[]}");

        assertThatThrownBy(() -> validator.validarSalvarRequest(request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O ID do tópico é obrigatório para salvar o rascunho de canvas.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se topicoId da request divergir do topicoIdResolvido")
    void deveLancarExceptionSeTopicoIdDivergir() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(id1, "{\"paths\":[]}");

        assertThatThrownBy(() -> validator.validarSalvarRequest(request, id2))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O ID do tópico na URL diverge do ID do tópico informado no corpo da requisição.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se dadosDesenhoJson for nulo ou em branco")
    void deveLancarExceptionSeDadosJsonVazio() {
        UUID topicoId = UUID.randomUUID();

        SalvarRascunhoCanvasRequest reqNulo = new SalvarRascunhoCanvasRequest(topicoId, null);
        assertThatThrownBy(() -> validator.validarSalvarRequest(reqNulo, topicoId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados do desenho do canvas são obrigatórios e não podem estar em branco.");

        SalvarRascunhoCanvasRequest reqBranco = new SalvarRascunhoCanvasRequest(topicoId, "   ");
        assertThatThrownBy(() -> validator.validarSalvarRequest(reqBranco, topicoId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados do desenho do canvas são obrigatórios e não podem estar em branco.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se dadosDesenhoJson exceder o limite de 5MB")
    void deveLancarExceptionSeExceder5MB() {
        UUID topicoId = UUID.randomUUID();
        String jsonGrande = "a".repeat(5_000_001);
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, jsonGrande);

        assertThatThrownBy(() -> validator.validarSalvarRequest(request, topicoId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O tamanho dos dados do canvas excede o limite máximo permitido de 5MB.");
    }

    @Test
    @DisplayName("Deve lancar BadRequestException se dadosDesenhoJson for um JSON malformado")
    void deveLancarExceptionSeJsonMalformado() {
        UUID topicoId = UUID.randomUUID();
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, "nao e um json {");

        assertThatThrownBy(() -> validator.validarSalvarRequest(request, topicoId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Os dados do desenho do canvas devem ser um JSON válido.");
    }

    @Test
    @DisplayName("Deve validar com sucesso quando topico pertence ao usuario")
    void deveValidarTopicoPertenceUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();

        PlanoEstudo plano = PlanoEstudo.builder().usuario(usuario).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).materia(materia).build();

        assertThatCode(() -> validator.validarTopicoPertenceUsuario(usuario, topico))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando topico pertence a outro usuario")
    void deveLancarForbiddenQuandoTopicoPertenceAOutroUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().usuario(outroUsuario).build();
        Materia materia = Materia.builder().planoEstudo(plano).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).materia(materia).build();

        assertThatThrownBy(() -> validator.validarTopicoPertenceUsuario(usuario, topico))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não tem permissão para associar rascunhos de canvas a este tópico.");
    }

    @Test
    @DisplayName("Deve validar com sucesso quando canvas pertence ao usuario")
    void deveValidarPropriedadeCanvas() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder().usuario(usuario).build();

        assertThatCode(() -> validator.validarPropriedadeCanvas(usuario, rascunho))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando canvas pertence a outro usuario")
    void deveLancarForbiddenQuandoCanvasPertenceAOutroUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();

        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder().usuario(outroUsuario).build();

        assertThatThrownBy(() -> validator.validarPropriedadeCanvas(usuario, rascunho))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para acessar este rascunho de canvas.");
    }
}
