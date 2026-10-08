package com.kyofoundation.skillnapse.modules.flashcard.support;

import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SrsAlgorithmSupportTest {

    private SrsAlgorithmSupport support;
    private LocalDate dataBase;

    @BeforeEach
    void setUp() {
        support = new SrsAlgorithmSupport();
        dataBase = LocalDate.of(2026, 10, 8);
    }

    @Test
    @DisplayName("Primeira revisao com ERRO deve definir repeticoes=0, intervalo=1 e reduzir EF")
    void deveProcessarPrimeiraRevisaoComErro() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(0)
                .repeticoes(0)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.ERRO, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(0);
        assertThat(resultado.intervaloDias()).isEqualTo(1);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(1));
        // 2.50 - 0.80 = 1.70
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("1.70");
    }

    @Test
    @DisplayName("Primeira revisao com BOM deve definir repeticoes=1, intervalo=1 e manter EF em 2.50")
    void deveProcessarPrimeiraRevisaoComBom() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(0)
                .repeticoes(0)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.BOM, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(1);
        assertThat(resultado.intervaloDias()).isEqualTo(1);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(1));
        // BOM (q=4): EF + 0.00 = 2.50
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Primeira revisao com FACIL deve definir repeticoes=1, intervalo=1 e aumentar EF para 2.60")
    void deveProcessarPrimeiraRevisaoComFacil() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(0)
                .repeticoes(0)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.FACIL, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(1);
        assertThat(resultado.intervaloDias()).isEqualTo(1);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(1));
        // FACIL (q=5): EF + 0.10 = 2.60
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("2.60");
    }

    @Test
    @DisplayName("Primeira revisao com DIFICIL deve definir repeticoes=1, intervalo=1 e reduzir EF para 2.36")
    void deveProcessarPrimeiraRevisaoComDificil() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(0)
                .repeticoes(0)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.DIFICIL, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(1);
        assertThat(resultado.intervaloDias()).isEqualTo(1);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(1));
        // DIFICIL (q=3): EF - 0.14 = 2.36
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("2.36");
    }

    @Test
    @DisplayName("Segunda revisao com BOM (repeticoes=1) deve definir repeticoes=2 e intervalo=6")
    void deveProcessarSegundaRevisaoComBom() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(1)
                .repeticoes(1)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.BOM, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(2);
        assertThat(resultado.intervaloDias()).isEqualTo(6);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(6));
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Terceira revisao com BOM (repeticoes=2, intervalo=6) deve multiplicar intervalo por EF (6 * 2.50 = 15)")
    void deveProcessarTerceiraRevisaoComBom() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(6)
                .repeticoes(2)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.BOM, dataBase);

        assertThat(resultado.repeticoes()).isEqualTo(3);
        assertThat(resultado.intervaloDias()).isEqualTo(15);
        assertThat(resultado.proximaRevisao()).isEqualTo(dataBase.plusDays(15));
        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("2.50");
    }

    @Test
    @DisplayName("Fator de facilidade nao deve ficar abaixo do limite minimo de 1.30")
    void naoDevePermitirFatorAbaixoDoMinimo() {
        Flashcard flashcard = Flashcard.builder()
                .fatorFacilidade(new BigDecimal("1.40"))
                .intervaloDias(10)
                .repeticoes(3)
                .build();

        var resultado = support.calcularProximaRevisao(flashcard, ClassificacaoResposta.ERRO, dataBase);

        assertThat(resultado.fatorFacilidade()).isEqualByComparingTo("1.30");
        assertThat(resultado.repeticoes()).isEqualTo(0);
        assertThat(resultado.intervaloDias()).isEqualTo(1);
    }
}
