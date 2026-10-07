package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TemplateSemanalResponse(
        UUID id,
        String nome,
        Boolean ativo,
        Instant criadoEm,
        Integer totalBlocos
) {
}
