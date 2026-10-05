package com.kyofoundation.skillnapse.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "O e-mail é obrigatório para realizar o login.")
        @NotNull(message = "O e-mail é obrigatório.")
        @Size(max = 150, message = "O e-mail não pode ter mais de 150 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória para realizar o login.")
        @NotNull(message = "A senha é obrigatória.")
        @Size(max = 72, message = "A senha não pode ter mais de 72 caracteres.")
        String senha
) {
}
