package com.kyofoundation.skillnapse.modules.questao.dto.projection;

import java.util.UUID;

public interface MetricasTopicoProjection {
    UUID getTopicoId();
    Long getTotalTentativas();
    Long getTotalAcertos();
    Double getTempoMedioSegundos();
}
