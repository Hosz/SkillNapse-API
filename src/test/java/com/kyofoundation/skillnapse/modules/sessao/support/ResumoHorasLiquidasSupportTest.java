package com.kyofoundation.skillnapse.modules.sessao.support;

import com.kyofoundation.skillnapse.modules.sessao.dto.response.ResumoHorasLiquidasResponse;
import com.kyofoundation.skillnapse.modules.sessao.repository.TotalizadorSessaoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResumoHorasLiquidasSupportTest {

    private ResumoHorasLiquidasSupport support;

    @BeforeEach
    void setUp() {
        support = new ResumoHorasLiquidasSupport();
    }

    @Test
    @DisplayName("Deve calcular resumo com conversao decimal de horas e totalizadores")
    void deveCalcularResumoCorretamente() {
        TotalizadorSessaoProjection dados = mock(TotalizadorSessaoProjection.class);
        when(dados.getTotalSegundos()).thenReturn(45000L); // 45000 / 3600 = 12.5 horas
        when(dados.getTotalConcluidas()).thenReturn(8L);
        when(dados.getTotalInterrompidas()).thenReturn(2L);

        ResumoHorasLiquidasResponse resumo = support.calcularResumo(dados);

        assertThat(resumo).isNotNull();
        assertThat(resumo.totalSegundosLiquidos()).isEqualTo(45000L);
        assertThat(resumo.totalHorasLiquidas()).isEqualTo(12.5);
        assertThat(resumo.totalSessoesConcluidas()).isEqualTo(8L);
        assertThat(resumo.totalSessoesInterrompidas()).isEqualTo(2L);
        assertThat(resumo.totalSessoes()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve retornar zeros quando dados brutos forem nulos")
    void deveRetornarZerosQuandoDadosNulos() {
        ResumoHorasLiquidasResponse resumo = support.calcularResumo(null);

        assertThat(resumo).isNotNull();
        assertThat(resumo.totalSegundosLiquidos()).isEqualTo(0L);
        assertThat(resumo.totalHorasLiquidas()).isEqualTo(0.0);
        assertThat(resumo.totalSessoes()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve tratar campos individuais nulos na projecao")
    void deveTratarCamposNulosNaProjecao() {
        TotalizadorSessaoProjection dados = mock(TotalizadorSessaoProjection.class);
        when(dados.getTotalSegundos()).thenReturn(null);
        when(dados.getTotalConcluidas()).thenReturn(null);
        when(dados.getTotalInterrompidas()).thenReturn(null);

        ResumoHorasLiquidasResponse resumo = support.calcularResumo(dados);

        assertThat(resumo).isNotNull();
        assertThat(resumo.totalSegundosLiquidos()).isEqualTo(0L);
        assertThat(resumo.totalHorasLiquidas()).isEqualTo(0.0);
        assertThat(resumo.totalSessoes()).isEqualTo(0L);
    }
}
