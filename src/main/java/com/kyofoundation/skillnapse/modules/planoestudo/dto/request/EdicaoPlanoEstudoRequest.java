package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import jakarta.validation.constraints.Size;

public record EdicaoPlanoEstudoRequest(
        @Size(max = 150, message = "O título não pode ter mais de 150 caracteres.")
        String titulo,
        String descricao,
        Boolean ativo
) {
}
