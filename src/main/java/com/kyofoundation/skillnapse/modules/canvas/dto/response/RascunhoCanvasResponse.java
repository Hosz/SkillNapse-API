package com.kyofoundation.skillnapse.modules.canvas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representação detalhada do rascunho de canvas acoplado a um tópico")
public record RascunhoCanvasResponse(

        @Schema(description = "Identificador único do rascunho de canvas", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "ID do usuário proprietário do rascunho", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        UUID usuarioId,

        @Schema(description = "ID do tópico ao qual o rascunho está acoplado", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID topicoId,

        @Schema(description = "Título do tópico acoplado", example = "Controle de Constitucionalidade")
        String topicoTitulo,

        @Schema(description = "Dados serializados em JSON vetorial contendo o desenho", example = "{\"version\":1,\"elements\":[]}")
        String dadosDesenhoJson,

        @Schema(description = "Timestamp UTC da última atualização do rascunho", example = "2026-10-08T12:00:00Z")
        Instant atualizadoEm
) {
}
