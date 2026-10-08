package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AtualizarTopicosRevisaoRequest(
        @NotEmpty(message = "A lista de tópicos para revisão não pode ser vazia.")
        @Schema(description = "Nova lista de identificadores dos tópicos a serem vinculados ao bloco")
        List<UUID> topicoIds
) {
}
