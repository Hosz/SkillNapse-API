package com.kyofoundation.skillnapse.modules.edital.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EditalUploadValidator {

    private static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB
    private static final String PDF_EXTENSION = ".pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    public void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BadRequestException("O arquivo de edital é obrigatório e não pode estar vazio.");
        }

        if (arquivo.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("O arquivo excede o tamanho máximo permitido de 25MB.");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.toLowerCase().endsWith(PDF_EXTENSION)) {
            throw new BadRequestException("O arquivo enviado deve possuir extensão .pdf.");
        }

        String contentType = arquivo.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase(PDF_CONTENT_TYPE) && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new BadRequestException("O tipo de mídia do arquivo deve ser application/pdf.");
        }
    }
}
