package com.kyofoundation.skillnapse.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotNull(message = "O nome é obrigatório.")
        @NotBlank(message = "É necessário um nome para se registrar.")
        @Size(max = 150, message = "O nome não pode ter mais de 150 caracteres.")
        String nome,

        @Email(message = "O e-mail informado possui um formato inválido.")
        @NotBlank(message = "É necessário um email para se registrar.")
        @NotNull(message = "O e-mail é obrigatório.")
        @Size(max = 150, message = "O e-mail não pode ter mais de 150 caracteres.")
        String email,

        @NotBlank(message = "É necessário definir uma senha para se registrar.")
        @NotNull(message = "A senha é obrigatória.")
        @Size(min = 6, max = 100, message = "A senha deve conter entre 6 e 100 caracteres.")
        String senha
) {
}
