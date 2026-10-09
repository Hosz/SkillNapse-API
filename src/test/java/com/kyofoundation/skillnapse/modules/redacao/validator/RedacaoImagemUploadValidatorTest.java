package com.kyofoundation.skillnapse.modules.redacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedacaoImagemUploadValidatorTest {

    private final RedacaoImagemUploadValidator validator = new RedacaoImagemUploadValidator();

    @Test
    @DisplayName("Deve validar imagem válida nos formatos suportados com sucesso")
    void deveValidarImagemComSucesso() {
        MockMultipartFile jpeg = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile png = new MockMultipartFile("imagem", "foto.png", "image/png", new byte[]{1, 2, 3});
        MockMultipartFile webp = new MockMultipartFile("imagem", "foto.webp", "image/webp", new byte[]{1, 2, 3});

        assertThatCode(() -> validator.validarImagem(jpeg)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarImagem(png)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarImagem(webp)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando imagem for nula ou vazia")
    void deveLancarQuandoImagemVazia() {
        assertThatThrownBy(() -> validator.validarImagem(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("obrigatório e não pode estar vazio");

        MockMultipartFile vazia = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", new byte[]{});
        assertThatThrownBy(() -> validator.validarImagem(vazia))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("obrigatório e não pode estar vazio");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando formato de imagem não for suportado")
    void deveLancarQuandoFormatoInvalido() {
        MockMultipartFile pdf = new MockMultipartFile("imagem", "arquivo.pdf", "application/pdf", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validarImagem(pdf))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Formato de imagem não suportado");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando tamanho ultrapassar 10MB")
    void deveLancarQuandoTamanhoExceder10MB() {
        byte[] payloadGrande = new byte[(10 * 1024 * 1024) + 1];
        MockMultipartFile grande = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", payloadGrande);

        assertThatThrownBy(() -> validator.validarImagem(grande))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pode exceder 10MB");
    }
}
