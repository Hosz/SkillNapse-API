package com.kyofoundation.skillnapse.modules.edital.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EditalArvoreEstruturada(
        @NotBlank
        String nomeConcurso,

        @NotBlank
        String cargo,

        @NotEmpty
        List<EditalMaterialItem> materias
) {
}
