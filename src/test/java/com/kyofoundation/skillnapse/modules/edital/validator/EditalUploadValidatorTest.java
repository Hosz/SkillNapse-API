package com.kyofoundation.skillnapse.modules.edital.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditalUploadValidatorTest {

    private EditalUploadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EditalUploadValidator();
    }

    @Test
    @DisplayName("Deve validar arquivo PDF com sucesso")
    void deveValidarArquivoPdfComSucesso() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "edital-bb-2026.pdf",
                "application/pdf",
                "%PDF-1.4 conteudo de teste".getBytes()
        );

        assertThatCode(() -> validator.validarArquivo(arquivo)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar se o arquivo for nulo ou vazio")
    void deveFalharSeArquivoNuloOuVazio() {
        assertThatThrownBy(() -> validator.validarArquivo(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("obrigatório");

        MockMultipartFile vazio = new MockMultipartFile("arquivo", "edital.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> validator.validarArquivo(vazio))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("Deve falhar se o arquivo nao tiver extensao .pdf")
    void deveFalharSeExtensaoNaoPdf() {
        MockMultipartFile txt = new MockMultipartFile("arquivo", "edital.txt", "text/plain", "texto".getBytes());
        assertThatThrownBy(() -> validator.validarArquivo(txt))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("extensão .pdf");
    }

    @Test
    @DisplayName("Deve falhar se o arquivo exceder 25MB")
    void deveFalharSeTamanhoExcederLimite() {
        byte[] payloadPesado = new byte[26 * 1024 * 1024]; // 26 MB
        MockMultipartFile pesado = new MockMultipartFile("arquivo", "edital.pdf", "application/pdf", payloadPesado);

        assertThatThrownBy(() -> validator.validarArquivo(pesado))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("25MB");
    }
}
