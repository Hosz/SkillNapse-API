package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import java.util.UUID;

public record TopicoRevisaoItemResponse(
        UUID id,
        String titulo,
        UUID materiaId,
        String materiaNome
) {
}
