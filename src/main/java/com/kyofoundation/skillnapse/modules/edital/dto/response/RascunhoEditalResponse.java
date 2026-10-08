package com.kyofoundation.skillnapse.modules.edital.dto.response;

import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalArvoreEstruturada;
import com.kyofoundation.skillnapse.modules.edital.enums.StatusRascunhoEdital;

import java.time.Instant;
import java.util.UUID;

public record RascunhoEditalResponse(
        UUID id,
        String nomeArquivo,
        UUID planoEstudoId,
        StatusRascunhoEdital status,
        EditalArvoreEstruturada conteudo,
        Instant criadoEm
) {
}
