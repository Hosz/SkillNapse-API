package com.kyofoundation.skillnapse.modules.redacao.support;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.stereotype.Component;

@Component
public class PromptRedacaoMultimodalSupport {

    public static final double LIMIAR_LEGIBILIDADE_MINIMO = 85.0;

    public PromptRequest construirPromptMultimodal(TemaRedacao tema) {
        String criterios = (tema.getCriteriosAvaliacao() != null && !tema.getCriteriosAvaliacao().isBlank())
                ? tema.getCriteriosAvaliacao().trim()
                : "Padrão dissertativo-argumentativo formal, coerência textual, extensão típica de 20 a 30 linhas.";

        String userPrompt = String.format("""
                Analise a imagem da folha de redação manuscrita enviada pelo candidato para o seguinte tema:
                - Tema: %s
                - Textos Motivadores:
                %s
                - Critérios da Proposta:
                %s

                DIRETRIZES DE PROCESSAMENTO MULTIMODAL:
                1. AVALIAÇÃO DE LEGIBILIDADE (Régua estrita de 85%%):
                   - Calcule o 'percentualLegibilidade' da caligrafia e nitidez geral (0.0 a 100.0%%).
                   - Se mais de 15.0%% do texto estiver ilegível, inacessível, borrado, com corte de margem ou iluminação precária (ou seja, se percentualLegibilidade < 85.0%%):
                     * Defina "legivel": false
                     * Forneça em "justificativaIlegibilidade" uma explicação detalhada dos pontos que inviabilizaram a leitura.
                     * Deixe "textoTranscrito" vazio e "correcao" nulo.
                   - Se percentualLegibilidade >= 85.0%%:
                     * Defina "legivel": true
                     * Em "textoTranscrito", realize a transcrição integral e fiel de todas as palavras escritas pelo aluno.
                     * Prossiga com a CORREÇÃO ANALÍTICA COMPLETA no objeto "correcao".

                2. CORREÇÃO ANALÍTICA COMPLETA (Padrão ENEM / 0 a 1000 pontos):
                   - Avalie as 5 competências essenciais, cada uma de 0.0 a 200.0 pontos:
                     * "Competência 1 - Domínio da Norma-Padrão"
                     * "Competência 2 - Compreensão do Tema e Repertório Sociocultural"
                     * "Competência 3 - Projeto de Texto e Estruturação Argumentativa"
                     * "Competência 4 - Mecanismos Linguísticos e Coesão Textual"
                     * "Competência 5 - Proposta de Intervenção e Conclusão"
                   - Calcule "notaGeral" na escala de 0.0 a 1000.0 pontos.
                   - Forneça "comentariosGerais" e identifique de 2 a 4 "sugestoesReescrita" a partir do texto transcrito.

                Retorne o resultado EXCLUSIVAMENTE no formato JSON com o seguinte schema:
                {
                  "legivel": true,
                  "percentualLegibilidade": 92.5,
                  "justificativaIlegibilidade": null,
                  "textoTranscrito": "string",
                  "correcao": {
                    "notaGeral": 880.0,
                    "competencias": [
                      {
                        "nomeCompetencia": "string",
                        "nota": 180.0,
                        "comentarios": "string",
                        "desviosIdentificados": ["string"]
                      }
                    ],
                    "comentariosGerais": "string",
                    "sugestoesReescrita": [
                      {
                        "trechoOriginal": "string",
                        "trechoSugerido": "string",
                        "justificativa": "string"
                      }
                    ]
                  }
                }
                """,
                tema.getTitulo(),
                tema.getTextosMotivadores(),
                criterios
        );

        String systemPrompt = "Você é um especialista em caligrafia, transcrição documental e banca examinadora de redações de concursos públicos e exames nacionais de excelência.";

        return new PromptRequest(
                userPrompt,
                systemPrompt,
                0.1,
                4000
        );
    }
}
