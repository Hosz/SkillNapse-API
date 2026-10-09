package com.kyofoundation.skillnapse.modules.questao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarAlternativaRequest(
        @NotBlank(message = "A letra da alternativa é obrigatória.")
        @Size(max = 1, message = "A letra da alternativa deve ter exatamente 1 caractere.")
        String letra,

        @NotBlank(message = "O texto da alternativa é obrigatório.")
        String texto,

        @NotNull(message = "A indicação se a alternativa é correta é obrigatória.")
        Boolean correta
) {
}
