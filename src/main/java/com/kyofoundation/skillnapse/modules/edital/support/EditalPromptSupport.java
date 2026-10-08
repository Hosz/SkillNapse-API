package com.kyofoundation.skillnapse.modules.edital.support;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import org.springframework.stereotype.Component;

@Component
public class EditalPromptSupport {

    private static final String SYSTEM_PROMPT = """
            Você é um especialista em análise pedagógica de editais de concursos públicos e vestibulares brasileiros da plataforma SkillNapse.
            Sua missão é identificar o concurso, o cargo e extrair com máxima fidelidade e organização a grade de Conteúdo Programático.
            
            Regras obrigatórias:
            1. Ignore termos puramente burocráticos (como isenção de taxa, locais de prova e cronograma de inscrições). Concentre-se nas disciplinas e conhecimentos exigidos.
            2. Divida claramente por disciplinas/matérias (ex: "Língua Portuguesa", "Direito Constitucional", "Raciocínio Lógico").
            3. Para cada tópico da disciplina, atribua um 'pesoEdital' inteiro de 1 a 5 baseado na relevância explícita ou padrão do edital (1 = introdutório/baixa incidência, 5 = matéria eliminatória/altíssima relevância).
            4. Caso o tópico possua desdobramentos, inclua-os na lista de 'subtopicos'.
            """;

    public PromptRequest criarPromptParaExtracao(String pdfExtraido) {
        String userPrompt = String.format("""
                Analise o seguinte texto extraído de um edital e construa a árvore hierárquica do conteúdo programático:
                
                --- INÍCIO DO TEXTO DO EDITAL ---
                %s
                --- FIM DO TEXTO DO EDITAL ---
                
                Extraia o nome do concurso, cargo e a lista completa de matérias e tópicos.
                """, pdfExtraido);

        return new PromptRequest(
                userPrompt,
                SYSTEM_PROMPT,
                0.1,
                4000
        );
    }
}
