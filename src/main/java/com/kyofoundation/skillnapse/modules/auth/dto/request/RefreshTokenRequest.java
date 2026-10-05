package com.kyofoundation.skillnapse.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para renovação de sessão via refresh token")
public record RefreshTokenRequest(

        @Schema(description = "Token de atualização", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "O token de atualização não pode ser nulo.")
        @NotBlank(message = "O token de atualização não pode ser vazio.")
        @Size(max = 255, message = "O token de atualização não pode ter mais de 255 caracteres.")
        String refreshToken
) {
}
