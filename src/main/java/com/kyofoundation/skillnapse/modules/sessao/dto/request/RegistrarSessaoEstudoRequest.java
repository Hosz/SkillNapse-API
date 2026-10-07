package com.kyofoundation.skillnapse.modules.sessao.dto.request;

import com.kyofoundation.skillnapse.modules.sessao.enums.StatusSessaoEstudo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record RegistrarSessaoEstudoRequest(
        @NotNull(message = "O ID do tópico é obrigatório.")
        UUID topicoId,

        @NotNull(message = "O timestamp de início é obrigatório.")
        @PastOrPresent(message = "O início da sessão não pode ser no futuro.")
        Instant iniciadoEm,

        @NotNull(message = "O timestamp de término é obrigatório.")
        Instant finalizadoEm,

        @NotNull(message = "A duração líquida em segundos é obrigatória.")
        @Positive(message = "A duração líquida deve ser maior que zero.")
        Integer duracaoLiquidaSegundos,

        @NotNull(message = "O status da sessão é obrigatório.")
        StatusSessaoEstudo status,

        @Size(max = 2000, message = "As observações não podem exceder 2000 caracteres.")
        String observacoes
) {
}
