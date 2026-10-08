package com.kyofoundation.skillnapse.modules.cronograma.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Proposta de cronograma semanal inteligente gerada pela IA")
public record SugestaoAutoAgendamentoResponse(
        @Schema(description = "Identificador do plano de estudo analisado")
        UUID planoEstudoId,

        @Schema(description = "Título do plano de estudo")
        String tituloPlano,

        @Schema(description = "Total de horas de estudo alocadas na semana")
        Double totalHorasSemanais,

        @Schema(description = "Total de blocos gerados pela IA")
        Integer totalBlocosSugeridos,

        @Schema(description = "Visão geral e orientação pedagógica da IA sobre a grade proposta")
        String resumoPedagogico,

        @Schema(description = "Lista detalhada de blocos de estudo distribuídos")
        List<BlocoSugeridoResponse> blocosSugeridos
) {
}
