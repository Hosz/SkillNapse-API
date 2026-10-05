package com.kyofoundation.skillnapse.modules.planoestudo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MateriaResponse(
        UUID id,
        String nome,
        String corHex,
        Integer ordem,
        Instant criadoEm
) {
}
