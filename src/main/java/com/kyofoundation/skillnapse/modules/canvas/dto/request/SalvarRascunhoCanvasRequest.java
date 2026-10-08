package com.kyofoundation.skillnapse.modules.canvas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Payload para salvar ou atualizar o rascunho de canvas acoplado a um tópico")
public record SalvarRascunhoCanvasRequest(

        @Schema(description = "ID do tópico acoplado (opcional se fornecido na URL)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID topicoId,

        @NotBlank(message = "Os dados do desenho do canvas são obrigatórios e não podem estar em branco.")
        @Schema(description = "Dados serializados em JSON vetorial contendo traços, ferramentas, coordenadas ou elementos", example = "{\"version\":1,\"elements\":[{\"type\":\"pen\",\"points\":[{\"x\":10,\"y\":20}]}]}")
        String dadosDesenhoJson
) {
}
