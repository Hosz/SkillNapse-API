package com.kyofoundation.skillnapse.qa;

import com.kyofoundation.skillnapse.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OpenApiDocsIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Deve retornar 200 OK na documentação OpenAPI /v3/api-docs contendo componentes de autenticação e rotas")
    void deveRetornarDocumentacaoOpenApiComSucesso() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("SkillNapse API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/plano-estudo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cronogramas/templates']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/sessoes-estudo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/baralhos']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/questoes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/redacoes/temas']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/redacoes/submissoes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/desempenho/planos/{planoId}/lacunas']").exists());
    }
}
