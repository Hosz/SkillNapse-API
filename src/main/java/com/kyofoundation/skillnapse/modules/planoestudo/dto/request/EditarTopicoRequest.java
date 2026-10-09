package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record EditarTopicoRequest(
        @Size(max = 200, message = "O título do tópico não pode ter mais de 200 caracteres.")
        String titulo,

        @Min(value = 1, message = "O peso no edital deve ser no mínimo 1.")
        @Max(value = 10, message = "O peso no edital deve ser no máximo 10.")
        Integer pesoEdital,

        NivelProficiencia nivelProficiencia,

        Boolean concluido,

        @Min(value = 0, message = "A ordem não pode ser negativa.")
        Integer ordem
) {
}
