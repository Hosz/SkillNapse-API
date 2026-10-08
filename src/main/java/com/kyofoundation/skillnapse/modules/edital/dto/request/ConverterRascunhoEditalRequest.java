package com.kyofoundation.skillnapse.modules.edital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConverterRascunhoEditalRequest(
        @NotBlank(message = "O título do plano é obrigatório.")
        @Size(max = 150)
        String tituloPlano,

        @Size(max = 500)
        String descricaoPlano
) {
}
