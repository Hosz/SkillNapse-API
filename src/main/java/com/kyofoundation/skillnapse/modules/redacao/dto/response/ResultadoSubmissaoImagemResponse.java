package com.kyofoundation.skillnapse.modules.redacao.dto.response;

public record ResultadoSubmissaoImagemResponse(
        boolean legivel,
        Double percentualLegibilidade,
        String mensagem,
        String textoTranscrito,
        SubmissaoRedacaoResponse submissao
) {
}
