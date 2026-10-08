package com.kyofoundation.skillnapse.modules.edital.dto.request;

import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalArvoreEstruturada;
import jakarta.validation.constraints.NotNull;

public record AtualizarRascunhoEditalRequest(
        @NotNull(message = "O conteúdo estruturado é obrigatório.")
        EditalArvoreEstruturada conteudo
) {
}
