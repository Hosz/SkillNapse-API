package com.kyofoundation.skillnapse.modules.sessao.dto.response;

public record ResumoHorasLiquidasResponse(
        Long totalSegundosLiquidos,
        Double totalHorasLiquidas,
        Long totalSessoesConcluidas,
        Long totalSessoesInterrompidas,
        Long totalSessoes
) {
}
