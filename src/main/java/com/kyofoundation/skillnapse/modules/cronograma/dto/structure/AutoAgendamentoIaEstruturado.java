package com.kyofoundation.skillnapse.modules.cronograma.dto.structure;

import java.util.List;

public record AutoAgendamentoIaEstruturado(
        String resumoPedagogico,
        List<BlocoIaEstruturado> blocos
) {
}
