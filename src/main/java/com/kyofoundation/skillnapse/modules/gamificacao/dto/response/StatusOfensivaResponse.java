package com.kyofoundation.skillnapse.modules.gamificacao.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record StatusOfensivaResponse(
        UUID id,
        UUID usuarioId,
        int diasConsecutivosAtual,
        int maiorSequenciaDias,
        LocalDate dataUltimoEstudo,
        boolean estudouHoje,
        boolean ofensivaAtiva
) {
}
