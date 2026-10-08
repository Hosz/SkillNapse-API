package com.kyofoundation.skillnapse.modules.edital.support;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EditalPromptSupportTest {

    @Test
    @DisplayName("Deve construir PromptRequest formatando texto do edital e system prompt")
    void deveConstruirPromptRequestCorretamente() {
        EditalPromptSupport support = new EditalPromptSupport();
        String texto = "Conteúdo Programático: Português, Matemática.";

        PromptRequest request = support.criarPromptParaExtracao(texto);

        assertThat(request).isNotNull();
        assertThat(request.prompt()).contains("Português, Matemática.");
        assertThat(request.prompt()).doesNotContain("$s");
        assertThat(request.systemMessage()).contains("Conteúdo Programático");
        assertThat(request.temperature()).isEqualTo(0.1);
        assertThat(request.maxTokens()).isEqualTo(4000);
    }
}
