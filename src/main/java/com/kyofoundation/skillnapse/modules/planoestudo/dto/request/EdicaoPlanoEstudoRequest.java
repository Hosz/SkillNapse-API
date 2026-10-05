package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

public record EdicaoPlanoEstudoRequest(
        String titulo,
        String descricao,
        Boolean ativo
) {
}
