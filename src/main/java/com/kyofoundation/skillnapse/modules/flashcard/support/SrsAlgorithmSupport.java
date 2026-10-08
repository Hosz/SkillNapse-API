package com.kyofoundation.skillnapse.modules.flashcard.support;

import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class SrsAlgorithmSupport {

    private static final BigDecimal FATOR_FACILIDADE_MINIMO = new BigDecimal("1.30");
    private static final BigDecimal FATOR_FACILIDADE_PADRAO = new BigDecimal("2.50");

    public record SrsResultadoCalculo(
            BigDecimal fatorFacilidade,
            Integer intervaloDias,
            Integer repeticoes,
            LocalDate proximaRevisao
    ) {
    }

    public SrsResultadoCalculo calcularProximaRevisao(Flashcard flashcard, ClassificacaoResposta classificacao) {
        return calcularProximaRevisao(flashcard, classificacao, LocalDate.now());
    }

    public SrsResultadoCalculo calcularProximaRevisao(Flashcard flashcard, ClassificacaoResposta classificacao, LocalDate dataReferencia) {
        LocalDate baseDate = dataReferencia != null ? dataReferencia : LocalDate.now();

        BigDecimal fatorAtual = (flashcard != null && flashcard.getFatorFacilidade() != null)
                ? flashcard.getFatorFacilidade()
                : FATOR_FACILIDADE_PADRAO;

        int repeticoesAtuais = (flashcard != null && flashcard.getRepeticoes() != null)
                ? flashcard.getRepeticoes()
                : 0;

        int intervaloAtual = (flashcard != null && flashcard.getIntervaloDias() != null)
                ? flashcard.getIntervaloDias()
                : 0;

        int qualidade = mapearQualidade(classificacao);

        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        double diferenca = 5 - qualidade;
        double variacaoEf = 0.1 - (diferenca * (0.08 + (diferenca * 0.02)));
        BigDecimal novoEf = fatorAtual.add(BigDecimal.valueOf(variacaoEf)).setScale(2, RoundingMode.HALF_UP);

        if (novoEf.compareTo(FATOR_FACILIDADE_MINIMO) < 0) {
            novoEf = FATOR_FACILIDADE_MINIMO;
        }

        int novasRepeticoes;
        int novoIntervaloDias;

        if (qualidade < 3) {
            // Falha na retenção: reinicia ciclo de repetições e agenda para o dia seguinte
            novasRepeticoes = 0;
            novoIntervaloDias = 1;
        } else {
            // Sucesso na retenção
            if (repeticoesAtuais == 0) {
                novoIntervaloDias = 1;
            } else if (repeticoesAtuais == 1) {
                novoIntervaloDias = 6;
            } else {
                long intervaloCalculado = Math.round(intervaloAtual * novoEf.doubleValue());
                novoIntervaloDias = (int) Math.max(1, intervaloCalculado);
            }
            novasRepeticoes = repeticoesAtuais + 1;
        }

        LocalDate novaProximaRevisao = baseDate.plusDays(novoIntervaloDias);

        return new SrsResultadoCalculo(novoEf, novoIntervaloDias, novasRepeticoes, novaProximaRevisao);
    }

    private int mapearQualidade(ClassificacaoResposta classificacao) {
        if (classificacao == null) {
            return 0;
        }
        return switch (classificacao) {
            case ERRO -> 0;
            case DIFICIL -> 3;
            case BOM -> 4;
            case FACIL -> 5;
        };
    }
}
