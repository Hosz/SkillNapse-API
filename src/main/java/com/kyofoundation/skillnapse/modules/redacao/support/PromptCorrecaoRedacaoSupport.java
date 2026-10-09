package com.kyofoundation.skillnapse.modules.redacao.support;

import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.stereotype.Component;

@Component
public class PromptCorrecaoRedacaoSupport {

    public String construirPromptCorrecao(TemaRedacao tema, String textoAluno) {
        String criterios = (tema.getCriteriosAvaliacao() != null && !tema.getCriteriosAvaliacao().isBlank())
                ? tema.getCriteriosAvaliacao().trim()
                : "Padrão dissertativo-argumentativo formal, coerência textual, extensão típica de 20 a 30 linhas.";

        return String.format("""
                Você é um examinador sênior de bancas de concurso público e exames nacionais de excelência (Cebraspe, FGV, FCC, ENEM).

                Sua atribuição é realizar a CORREÇÃO ANALÍTICA E PEDAGÓGICA RIGOROSA da redação discursiva submetida pelo candidato.

                DADOS DA PROPOSTA DE REDAÇÃO:
                - Tema / Título: %s
                - Textos Motivadores:
                %s
                - Critérios e Itens Norteadores da Proposta:
                %s

                TEXTO REDIGIDO PELO CANDIDATO:
                \"\"\"
                %s
                \"\"\"

                INSTRUÇÕES E DIRETRIZES DA CORREÇÃO:
                1. Avalie detalhadamente as 4 competências fundamentais, atribuindo a cada uma nota de 0.0 a 10.0:
                   - "Gramática e Norma-Padrão": Ortografia, concordância, regência, crase, pontuação e precisão vocabular. Liste desvios específicos encontrados.
                   - "Coesão e Coerência": Articulação lógica, uso adequado de conectivos inter e intraparágrafos, progressão temática sem repetições viciosas.
                   - "Estrutura Dissertativa e Argumentação": Defesa clara da tese, fundamentação com repertório sociocultural legitimado, coerência argumentativa.
                   - "Atendimento ao Tema e Itens da Proposta": Cumprimento integral do recorte temático e das orientações formais solicitadas.
                2. Calcule a "notaGeral" na escala de 0.0 a 10.0 (com até duas casas decimais).
                3. Em "comentariosGerais", apresente um diagnóstico pedagógico consolidado destacando pontos fortes e principais fragilidades a corrigir.
                4. Em "sugestoesReescrita", identifique de 2 a 4 trechos reais do texto do candidato que apresentem truncamento, informalidade ou fragilidade sintática/argumentativa, fornecendo a versão aprimorada e a fundamentação explicativa.

                Retorne o resultado EXCLUSIVAMENTE em formato JSON com o seguinte schema:
                {
                  "notaGeral": 0.0,
                  "competencias": [
                    {
                      "nomeCompetencia": "string",
                      "nota": 0.0,
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
                """,
                tema.getTitulo(),
                tema.getTextosMotivadores(),
                criterios,
                textoAluno.trim()
        );
    }
}
