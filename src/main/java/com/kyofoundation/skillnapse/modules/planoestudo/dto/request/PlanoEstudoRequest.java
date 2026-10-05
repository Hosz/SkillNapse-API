package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanoEstudoRequest(

        @NotNull
        @NotBlank
        String titulo,

        @NotNull
        @NotBlank
        String descricao
) {
}
