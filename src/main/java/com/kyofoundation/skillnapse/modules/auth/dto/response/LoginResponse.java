package com.kyofoundation.skillnapse.modules.auth.dto.response;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String nome,
        String email,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
