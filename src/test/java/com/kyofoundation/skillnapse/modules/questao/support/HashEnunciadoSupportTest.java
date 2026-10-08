package com.kyofoundation.skillnapse.modules.questao.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashEnunciadoSupportTest {

    private HashEnunciadoSupport hashSupport;

    @BeforeEach
    void setUp() {
        hashSupport = new HashEnunciadoSupport();
    }

    @Test
    @DisplayName("Deve gerar o mesmo hash para enunciados com variações de maiúsculas e espaços")
    void deveGerarMesmoHashParaEnunciadosNormalizaveis() {
        String enunciado1 = "Qual é a capital do Brasil? ";
        String enunciado2 = "   qual  é  a   capital  do   brasil?  ";

        String hash1 = hashSupport.gerarHash(enunciado1);
        String hash2 = hashSupport.gerarHash(enunciado2);

        assertThat(hash1).isNotEmpty();
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Deve gerar hashes diferentes para enunciados distintos")
    void deveGerarHashesDiferentesParaTextosDistintos() {
        String hash1 = hashSupport.gerarHash("O que é Java?");
        String hash2 = hashSupport.gerarHash("O que é Python?");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("Deve retornar string vazia para enunciado nulo ou em branco")
    void deveRetornarVazioParaTextoNuloOuBranco() {
        assertThat(hashSupport.gerarHash(null)).isEmpty();
        assertThat(hashSupport.gerarHash("   ")).isEmpty();
    }
}
