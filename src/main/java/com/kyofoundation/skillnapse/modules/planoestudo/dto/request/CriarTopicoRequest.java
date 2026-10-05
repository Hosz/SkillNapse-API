package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CriarTopicoRequest(
        @NotBlank(message = "O título do tópico é obrigatório.")
        @Size(max = 200, message = "O título do tópico não pode ter mais de 200 caracteres.")
        String titulo,

        UUID topicoPaiId,

        @Min(value = 1, message = "O peso no edital deve ser no mínimo 1.")
        @Max(value = 5, message = "O peso no edital deve ser no máximo 5.")
        Integer pesoEdital,

        NivelProficiencia nivelProficiencia,

        @Min(value = 0, message = "A ordem não pode ser negativa.")
        Integer ordem
) {
}
