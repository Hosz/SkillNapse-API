package com.kyofoundation.skillnapse.modules.sessao.repository;

public interface TotalizadorSessaoProjection {
    Long getTotalSegundos();
    Long getTotalConcluidas();
    Long getTotalInterrompidas();
}
