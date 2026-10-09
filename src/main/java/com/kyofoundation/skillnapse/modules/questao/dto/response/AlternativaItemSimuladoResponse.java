package com.kyofoundation.skillnapse.modules.questao.dto.response;

import java.util.UUID;

public record AlternativaItemSimuladoResponse(
        UUID id,
        String letra,
        String texto
) {
}
