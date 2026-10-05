package com.kyofoundation.skillnapse.modules.auth.dto.response;

import java.util.UUID;

public record RegistroResponse(
        UUID id,
        String nome,
        String email
) {
}
