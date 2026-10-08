package com.kyofoundation.skillnapse.modules.edital.support;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfTextExtractorSupportTest {

    private PdfTextExtractorSupport extractor;

    @BeforeEach
    void setUp() {
        extractor = new PdfTextExtractorSupport();
    }

    private byte[] gerarPdfValido(String texto) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.showText(texto);
                content.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Deve extrair texto de um PDF valido com sucesso")
    void deveExtrairTextoDePdfValido() throws IOException {
        byte[] pdfBytes = gerarPdfValido("Edital Concurso Publico");
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "edital.pdf", "application/pdf", pdfBytes);

        String texto = extractor.extrairTexto(arquivo);

        assertThat(texto).contains("Edital Concurso Publico");
    }

    @Test
    @DisplayName("Deve falhar ao tentar extrair de arquivo vazio ou nulo")
    void deveFalharComArquivoVazio() {
        assertThatThrownBy(() -> extractor.extrairTexto(null))
                .isInstanceOf(BadRequestException.class);

        MockMultipartFile vazio = new MockMultipartFile("arquivo", "edital.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> extractor.extrairTexto(vazio))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve falhar com bytes corrompidos")
    void deveFalharComBytesCorrompidos() {
        MockMultipartFile corrompido = new MockMultipartFile("arquivo", "edital.pdf", "application/pdf", "nao_e_pdf".getBytes());
        assertThatThrownBy(() -> extractor.extrairTexto(corrompido))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("corrompido");
    }
}
