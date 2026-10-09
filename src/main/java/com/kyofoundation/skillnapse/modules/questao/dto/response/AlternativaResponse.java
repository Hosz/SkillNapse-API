package com.kyofoundation.skillnapse.modules.questao.dto.response;

import java.util.UUID;

public record AlternativaResponse(
        UUID id,
        String letra,
        String texto,
        Boolean correta
) {
}
