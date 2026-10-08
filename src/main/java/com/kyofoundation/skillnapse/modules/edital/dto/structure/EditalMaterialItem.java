package com.kyofoundation.skillnapse.modules.edital.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EditalMaterialItem(
        @NotBlank
        String nome,

        @NotEmpty
        List<EditalTopicoItem> topicos
) {
}
