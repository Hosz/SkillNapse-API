package com.kyofoundation.skillnapse.modules.gamificacao.support;

import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalculoOfensivaSupportTest {

    private CalculoOfensivaSupport support;
    private OfensivaUsuario ofensiva;

    @BeforeEach
    void setUp() {
        support = new CalculoOfensivaSupport();
        ofensiva = OfensivaUsuario.builder()
                .id(UUID.randomUUID())
                .diasConsecutivosAtual(0)
                .maiorSequenciaDias(0)
                .dataUltimoEstudo(null)
                .build();
    }

    @Test
    @DisplayName("[registrarEstudo] Primeiro estudo deve iniciar a ofensiva com 1 dia")
    void deveIniciarOfensivaNoPrimeiroEstudo() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);

        support.registrarEstudo(ofensiva, hoje);

        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(1);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(1);
        assertThat(ofensiva.getDataUltimoEstudo()).isEqualTo(hoje);
    }

    @Test
    @DisplayName("[registrarEstudo] Múltiplos estudos no mesmo dia não devem duplicar incremento")
    void naoDeveIncrementarEstudosNoMesmoDia() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);

        support.registrarEstudo(ofensiva, hoje);
        support.registrarEstudo(ofensiva, hoje);

        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(1);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(1);
    }

    @Test
    @DisplayName("[registrarEstudo] Estudo em dia imediatamente consecutivo deve incrementar a ofensiva")
    void deveIncrementarOfensivaEmDiaConsecutivo() {
        LocalDate ontem = LocalDate.of(2026, 10, 8);
        LocalDate hoje = LocalDate.of(2026, 10, 9);

        support.registrarEstudo(ofensiva, ontem);
        support.registrarEstudo(ofensiva, hoje);

        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(2);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(2);
        assertThat(ofensiva.getDataUltimoEstudo()).isEqualTo(hoje);
    }

    @Test
    @DisplayName("[registrarEstudo] Quebra de sequência por hiato de 2 dias deve reiniciar ofensiva em 1 mantendo recorde")
    void deveReiniciarOfensivaAposHiatoMantendoRecorde() {
        LocalDate dia1 = LocalDate.of(2026, 10, 1);
        LocalDate dia2 = LocalDate.of(2026, 10, 2);
        LocalDate dia3 = LocalDate.of(2026, 10, 3);
        LocalDate dia6 = LocalDate.of(2026, 10, 6); // pulou dias 4 e 5

        support.registrarEstudo(ofensiva, dia1);
        support.registrarEstudo(ofensiva, dia2);
        support.registrarEstudo(ofensiva, dia3);

        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(3);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(3);

        support.registrarEstudo(ofensiva, dia6);

        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(1);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(3);
        assertThat(ofensiva.getDataUltimoEstudo()).isEqualTo(dia6);
    }

    @Test
    @DisplayName("[revalidarStreak] Deve zerar ofensiva atual se o último estudo for anterior a ontem")
    void deveZerarOfensivaSeSequenciaFoiRompida() {
        LocalDate tresDiasAtras = LocalDate.of(2026, 10, 5);
        LocalDate hoje = LocalDate.of(2026, 10, 8);

        ofensiva.setDiasConsecutivosAtual(5);
        ofensiva.setMaiorSequenciaDias(10);
        ofensiva.setDataUltimoEstudo(tresDiasAtras);

        boolean quebra = support.revalidarStreak(ofensiva, hoje);

        assertThat(quebra).isTrue();
        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(0);
        assertThat(ofensiva.getMaiorSequenciaDias()).isEqualTo(10);
    }

    @Test
    @DisplayName("[revalidarStreak] Não deve alterar se o último estudo foi ontem ou hoje")
    void naoDeveZerarSeEstudouOntemOuHoje() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        LocalDate ontem = LocalDate.of(2026, 10, 7);

        ofensiva.setDiasConsecutivosAtual(4);
        ofensiva.setDataUltimoEstudo(ontem);

        boolean quebra = support.revalidarStreak(ofensiva, hoje);

        assertThat(quebra).isFalse();
        assertThat(ofensiva.getDiasConsecutivosAtual()).isEqualTo(4);

        ofensiva.setDataUltimoEstudo(hoje);
        assertThat(support.revalidarStreak(ofensiva, hoje)).isFalse();
    }

    @Test
    @DisplayName("[isEstudouHoje / isOfensivaAtiva] Deve checar estados corretamente")
    void deveChecarEstadosDeEstudoEOfensivaAtiva() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        LocalDate ontem = LocalDate.of(2026, 10, 7);

        assertThat(support.isEstudouHoje(ofensiva, hoje)).isFalse();
        assertThat(support.isOfensivaAtiva(ofensiva, hoje)).isFalse();

        ofensiva.setDiasConsecutivosAtual(3);
        ofensiva.setDataUltimoEstudo(ontem);

        assertThat(support.isEstudouHoje(ofensiva, hoje)).isFalse();
        assertThat(support.isOfensivaAtiva(ofensiva, hoje)).isTrue(); // ativa pois ainda pode estudar hoje!

        ofensiva.setDataUltimoEstudo(hoje);
        assertThat(support.isEstudouHoje(ofensiva, hoje)).isTrue();
        assertThat(support.isOfensivaAtiva(ofensiva, hoje)).isTrue();
    }
}
