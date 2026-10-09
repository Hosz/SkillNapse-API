package com.kyofoundation.skillnapse.modules.redacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class RedacaoImagemUploadValidator {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    public void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new BadRequestException("O arquivo de imagem da redação manuscrita é obrigatório e não pode estar vazio.");
        }

        if (imagem.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("O tamanho da imagem da redação não pode exceder 10MB.");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Formato de imagem não suportado. Utilize imagens nos formatos JPEG, PNG ou WEBP.");
        }
    }
}
