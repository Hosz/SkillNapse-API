package com.kyofoundation.skillnapse.modules.desempenho.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.PainelGeralDesempenhoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.RelatorioLacunasResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.TopicoCriticoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.enums.NivelCriticidadeTopico;
import com.kyofoundation.skillnapse.modules.desempenho.service.DesempenhoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesempenhoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DesempenhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DesempenhoService desempenhoService;

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar relatorio de lacunas autenticado")
    void deveObterRelatorioLacunasComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        RelatorioLacunasResponse response = new RelatorioLacunasResponse(
                planoId,
                "Plano Receita Federal",
                72.5,
                150L,
                110L,
                73.3,
                2,
                List.of()
        );

        when(desempenhoService.obterRelatorioLacunas(eq(userId), eq(planoId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/desempenho/planos/{planoId}/lacunas", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planoId").value(planoId.toString()))
                .andExpect(jsonPath("$.planoTitulo").value("Plano Receita Federal"))
                .andExpect(jsonPath("$.scoreProntidao").value(72.5))
                .andExpect(jsonPath("$.totalQuestoesRespondidas").value(150))
                .andExpect(jsonPath("$.totalTopicosCriticos").value(2));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando plano de estudo nao for encontrado")
    void deveRetornar404QuandoPlanoNaoEncontrado() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        when(desempenhoService.obterRelatorioLacunas(eq(userId), eq(planoId)))
                .thenThrow(new ResourceNotFoundException("O plano de estudo indicado não foi encontrado."));

        mockMvc.perform(get("/api/v1/desempenho/planos/{planoId}/lacunas", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("O plano de estudo indicado não foi encontrado."));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando plano pertencer a outro usuario")
    void deveRetornar403QuandoPlanoDeOutroUsuario() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        when(desempenhoService.obterRelatorioLacunas(eq(userId), eq(planoId)))
                .thenThrow(new ForbiddenException("Você não possui permissão para acessar os dados analíticos deste plano de estudo."));

        mockMvc.perform(get("/api/v1/desempenho/planos/{planoId}/lacunas", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Você não possui permissão para acessar os dados analíticos deste plano de estudo."));
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar ranking de topicos criticos")
    void deveObterTopicosCriticosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        TopicoCriticoResponse critico = new TopicoCriticoResponse(
                topicoId,
                "Direito Administrativo - Licitações",
                UUID.randomUUID(),
                "Direito Administrativo",
                5,
                40.0,
                10L,
                300.0,
                NivelCriticidadeTopico.CRITICO
        );

        when(desempenhoService.obterTopicosCriticos(eq(userId), eq(planoId), eq(5)))
                .thenReturn(List.of(critico));

        mockMvc.perform(get("/api/v1/desempenho/planos/{planoId}/topicos-criticos", planoId)
                        .param("limite", "5")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].topicoId").value(topicoId.toString()))
                .andExpect(jsonPath("$[0].tituloTopico").value("Direito Administrativo - Licitações"))
                .andExpect(jsonPath("$[0].criticidade").value("CRITICO"))
                .andExpect(jsonPath("$[0].indiceSeveridade").value(300.0));
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar painel geral consolidado")
    void deveObterPainelGeralComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();

        PainelGeralDesempenhoResponse painel = new PainelGeralDesempenhoResponse(
                200L,
                150L,
                75.0,
                45.2,
                1,
                List.of(),
                List.of()
        );

        when(desempenhoService.obterPainelGeral(eq(userId))).thenReturn(painel);

        mockMvc.perform(get("/api/v1/desempenho/geral")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestoesRespondidas").value(200))
                .andExpect(jsonPath("$.taxaAcertoGeral").value(75.0));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando requisicao nao possuir token JWT")
    void deveRetornar401SemJwt() throws Exception {
        mockMvc.perform(get("/api/v1/desempenho/geral")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
