package com.kyofoundation.skillnapse.modules.questao.dto.payload;

public record AlternativaGeradaIaPayload(
        String texto,
        Boolean correta,
        Integer ordem
) {
}
