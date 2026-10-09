package com.kyofoundation.skillnapse.modules.gamificacao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.MetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.PainelGamificacaoResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.ProgressoMetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.service.MetaDiariaService;
import com.kyofoundation.skillnapse.modules.gamificacao.service.OfensivaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GamificacaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class GamificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private OfensivaService ofensivaService;

    @MockitoBean
    private MetaDiariaService metaDiariaService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("[GET /painel] Deve retornar painel consolidado com 200 OK")
    void deveRetornarPainelConsolidado() throws Exception {
        StatusOfensivaResponse ofensiva = new StatusOfensivaResponse(
                UUID.randomUUID(), userId, 4, 7, LocalDate.now(), true, true
        );
        ProgressoMetaDiariaResponse progresso = new ProgressoMetaDiariaResponse(
                LocalDate.now(), 120, 90L, 75.0, false, 15, 10L, 66.7, false, false
        );
        PainelGamificacaoResponse painel = new PainelGamificacaoResponse(ofensiva, progresso);

        when(metaDiariaService.obterPainelCompleto(eq(userId), any())).thenReturn(painel);

        mockMvc.perform(get("/api/v1/gamificacao/painel")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofensiva.diasConsecutivosAtual").value(4))
                .andExpect(jsonPath("$.progressoHoje.minutosEstudadosHoje").value(90));
    }

    @Test
    @DisplayName("[GET /ofensiva] Deve retornar status da ofensiva com 200 OK")
    void deveRetornarStatusOfensiva() throws Exception {
        StatusOfensivaResponse response = new StatusOfensivaResponse(
                UUID.randomUUID(), userId, 3, 5, LocalDate.now().minusDays(1), false, true
        );

        when(ofensivaService.obterStatusOfensiva(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamificacao/ofensiva")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasConsecutivosAtual").value(3))
                .andExpect(jsonPath("$.estudouHoje").value(false))
                .andExpect(jsonPath("$.ofensivaAtiva").value(true));
    }

    @Test
    @DisplayName("[POST /ofensiva/registrar-estudo] Deve registrar estudo e retornar status com 200 OK")
    void deveRegistrarEstudoOfensiva() throws Exception {
        LocalDate hoje = LocalDate.now();
        StatusOfensivaResponse response = new StatusOfensivaResponse(
                UUID.randomUUID(), userId, 4, 5, hoje, true, true
        );

        when(ofensivaService.registrarEstudo(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/gamificacao/ofensiva/registrar-estudo")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .param("dataEstudo", hoje.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasConsecutivosAtual").value(4))
                .andExpect(jsonPath("$.estudouHoje").value(true));
    }

    @Test
    @DisplayName("[GET /metas] Deve retornar configuração das metas com 200 OK")
    void deveRetornarConfiguracaoMetas() throws Exception {
        MetaDiariaResponse response = new MetaDiariaResponse(
                UUID.randomUUID(), userId, 120, 15
        );

        when(metaDiariaService.obterConfiguracaoMetas(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamificacao/metas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMinutosEstudo").value(120))
                .andExpect(jsonPath("$.metaQuestoesResolvidas").value(15));
    }

    @Test
    @DisplayName("[PUT /metas] Deve atualizar metas com 200 OK")
    void deveAtualizarMetasComSucesso() throws Exception {
        AtualizarMetaDiariaRequest request = new AtualizarMetaDiariaRequest(150, 20);
        MetaDiariaResponse response = new MetaDiariaResponse(
                UUID.randomUUID(), userId, 150, 20
        );

        when(metaDiariaService.atualizarMetas(eq(userId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/gamificacao/metas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMinutosEstudo").value(150))
                .andExpect(jsonPath("$.metaQuestoesResolvidas").value(20));
    }

    @Test
    @DisplayName("[GET /metas/progresso] Deve retornar progresso diário com 200 OK")
    void deveRetornarProgressoDiario() throws Exception {
        LocalDate hoje = LocalDate.now();
        ProgressoMetaDiariaResponse response = new ProgressoMetaDiariaResponse(
                hoje, 120, 120L, 100.0, true, 15, 18L, 100.0, true, true
        );

        when(metaDiariaService.obterProgressoDiario(eq(userId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/gamificacao/metas/progresso")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metaMinutosAtingida").value(true))
                .andExpect(jsonPath("$.todasMetasAtingidas").value(true));
    }

    @Test
    @DisplayName("[PUT /metas] Deve retornar 400 Bad Request se valores forem inválidos")
    void deveRetornarBadRequestSeMetasInvalidas() throws Exception {
        AtualizarMetaDiariaRequest requestInvalido = new AtualizarMetaDiariaRequest(0, -5);

        mockMvc.perform(put("/api/v1/gamificacao/metas")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
