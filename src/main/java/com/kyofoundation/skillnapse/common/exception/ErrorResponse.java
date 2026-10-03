package com.kyofoundation.skillnapse.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Estrutura padronizada para retorno de erros da API")
public record ErrorResponse(
    @Schema(description = "Código de status HTTP", example = "400")
    int status,

    @Schema(description = "Descrição breve do tipo de erro", example = "Bad Request")
    String error,

    @Schema(description = "Mensagem detalhada sobre o motivo do erro", example = "O prompt não pode ser nulo ou vazio.")
    String message,

    @Schema(description = "Momento UTC em que o erro ocorreu")
    Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
