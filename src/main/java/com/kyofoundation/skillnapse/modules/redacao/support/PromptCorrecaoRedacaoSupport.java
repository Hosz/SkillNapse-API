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
                1. Avalie detalhadamente as 5 competências fundamentais (padrão ENEM / bancas examinadoras de elite), atribuindo a cada uma nota de 0.0 a 200.0:
                   - "Competência 1 - Domínio da Norma-Padrão": Ortografia, acentuação, concordância, regência, crase, pontuação e precisão vocabular. Liste desvios específicos encontrados.
                   - "Competência 2 - Compreensão do Tema e Aplicação de Repertório": Interpretação do tema, abordagem sem tangenciamento e uso produtivo de repertório sociocultural legitimado.
                   - "Competência 3 - Projeto de Texto e Estruturação Argumentativa": Organização lógica das ideias, defesa consistente do ponto de vista e articulação dos argumentos.
                   - "Competência 4 - Mecanismos Linguísticos e Coesão Textual": Conectivos inter e intraparágrafos, progressão referencial e sequencial sem repetições viciosas.
                   - "Competência 5 - Proposta de Intervenção e Conclusão": Elaboração de solução ou conclusão articulada com os agentes, ações, meios e efeitos pertinentes à temática.
                2. Calcule a "notaGeral" na escala de 0.0 a 1000.0 (soma das competências, com até duas casas decimais).
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
