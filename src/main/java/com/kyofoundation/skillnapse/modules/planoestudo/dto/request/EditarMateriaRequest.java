package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarMateriaRequest(
        @Size(max = 100, message = "O nome da matéria não pode ter mais de 100 caracteres.")
        String nome,

        String corHex,

        Integer ordem
) {
}
