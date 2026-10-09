package com.kyofoundation.skillnapse.modules.redacao.support;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

@Component
public class PromptTemaRedacaoSupport {

    public String construirPromptGeracao(
            PlanoEstudo plano,
            Materia materia,
            Topico topico,
            String bancaAlvo,
            String generoTextual
    ) {
        String banca = (bancaAlvo != null && !bancaAlvo.isBlank())
                ? bancaAlvo.trim()
                : "Bancas Tradicionais de Concurso Público (Cebraspe/FGV/FCC)";

        String genero = (generoTextual != null && !generoTextual.isBlank())
                ? generoTextual.trim()
                : "Dissertativo-Argumentativo";

        String materiaNome = (materia != null && materia.getNome() != null)
                ? materia.getNome().trim()
                : "Geral / Conhecimentos Interdisciplinares";

        String topicoNome = (topico != null && topico.getTitulo() != null)
                ? topico.getTitulo().trim()
                : "Temas Contemporâneos e Relevantes ao Cargo";

        return String.format("""
                Você é um membro sênior de banca examinadora responsável pela elaboração de provas discursivas e redações de alto nível para concursos públicos e seleções acadêmicas.

                Sua missão é formular uma PROPOSTA INÉDITA E COMPLETA DE REDAÇÃO orientada aos seguintes parâmetros:
                - Plano de Estudo / Concurso Alvo: %s
                - Disciplina / Eixo Temático: %s
                - Tópico Específico em Foco: %s
                - Gênero Textual Exigido: %s
                - Estilo da Banca Avaliadora: %s

                REQUISITOS ESTRITOS DA PROPOSTA:
                1. "titulo": Deve ser um tema provocativo, relevante, claro e delimitado (entre 10 e 150 caracteres).
                2. "textosMotivadores": Deve conter uma coletânea realista de 2 a 3 textos motivadores independentes (Excertos de Leis/Jurisprudência, dados estatísticos de órgãos confiáveis, reflexões conceituais ou notícias factuais). Use marcações de texto elegantes (Ex: Texto I, Texto II, Texto III).
                3. "criteriosAvaliacao": Deve especificar as orientações formais da prova (extensão recomendada de 20 a 30 linhas, norma-padrão da língua portuguesa) e listar de 2 a 3 tópicos de abordagem obrigatória (itens norteadores de pontuação).

                Retorne o resultado EXCLUSIVAMENTE em formato JSON com o seguinte schema:
                {
                  "titulo": "string",
                  "textosMotivadores": "string",
                  "criteriosAvaliacao": "string"
                }
                """,
                plano.getTitulo(),
                materiaNome,
                topicoNome,
                genero,
                banca
        );
    }
}
