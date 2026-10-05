package com.kyofoundation.skillnapse.modules.planoestudo.dto.request;

import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;

public record AtualizarProgressoRequest(
        Boolean concluido,
        NivelProficiencia nivelProficiencia
) {
}
