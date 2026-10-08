package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Confirmação de aplicação e persistência do auto-agendamento no template semanal")
public record ResultadoAutoAgendamentoResponse(
        @Schema(description = "Identificador do template semanal onde os blocos foram persistidos")
        UUID templateSemanalId,

        @Schema(description = "Nome do template semanal")
        String nomeTemplate,

        @Schema(description = "Quantidade de blocos persistidos com sucesso")
        Integer totalBlocosPersistidos,

        @Schema(description = "Resumo e justificativa pedagógica da distribuição gerada")
        String resumoPedagogico,

        @Schema(description = "Lista dos blocos salvos na base de dados")
        List<BlocoSugeridoResponse> blocosPersistidos
) {
}
