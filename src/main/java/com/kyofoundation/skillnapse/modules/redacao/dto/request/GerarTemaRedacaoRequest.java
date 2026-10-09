package com.kyofoundation.skillnapse.modules.redacao.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GerarTemaRedacaoRequest(
        @NotNull(message = "O ID do plano de estudo é obrigatório.")
        UUID planoEstudoId,

        UUID materiaId,

        UUID topicoId,

        @Size(max = 50, message = "O nome da banca avaliadora não pode ultrapassar 50 caracteres.")
        String bancaAlvo,

        @Size(max = 50, message = "O gênero textual não pode ultrapassar 50 caracteres.")
        String generoTextual
) {
    public String generoTextualEfetivo() {
        return (generoTextual != null && !generoTextual.isBlank())
                ? generoTextual.trim()
                : "Dissertativo-Argumentativo";
    }
}
