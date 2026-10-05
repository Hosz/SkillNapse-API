package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarMateriaRequest(
        @NotNull(message = "O nome da matéria é obrigatório.")
        @NotBlank(message = "O nome da matéria não pode ser vazio.")
        @Size(max = 100, message = "O nome da matéria não pode ter mais de 100 caracteres.")
        String nome,

        @Size(max = 7, message = "A cor hexadecimal não pode ter mais de 7 caracteres.")
        String corHex,

        Integer ordem
) {
}
