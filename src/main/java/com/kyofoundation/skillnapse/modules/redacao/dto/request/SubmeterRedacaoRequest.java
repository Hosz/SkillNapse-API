package com.kyofoundation.skillnapse.modules.redacao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SubmeterRedacaoRequest(
        @NotNull(message = "O ID do tema de redação é obrigatório.")
        UUID temaRedacaoId,

        @NotBlank(message = "O texto da redação não pode estar vazio.")
        @Size(min = 300, max = 5000, message = "O texto da redação deve ter entre 300 e 5000 caracteres.")
        String textoAluno
) {
}
