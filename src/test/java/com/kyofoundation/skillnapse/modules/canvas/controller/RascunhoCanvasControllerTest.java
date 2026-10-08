package com.kyofoundation.skillnapse.modules.canvas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.service.RascunhoCanvasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RascunhoCanvasController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RascunhoCanvasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private RascunhoCanvasService rascunhoCanvasService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve salvar rascunho de canvas por topico com status 200 OK via PUT")
    void deveSalvarRascunhoPorTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        String json = "{\"paths\":[]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, json);
        RascunhoCanvasResponse response = new RascunhoCanvasResponse(
                canvasId, userId, topicoId, "Tópico de Estudo", json, Instant.now()
        );

        when(rascunhoCanvasService.salvarRascunho(eq(userId), eq(topicoId), any(SalvarRascunhoCanvasRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/canvas/topico/{topicoId}", topicoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canvasId.toString()))
                .andExpect(jsonPath("$.usuarioId").value(userId.toString()))
                .andExpect(jsonPath("$.topicoId").value(topicoId.toString()))
                .andExpect(jsonPath("$.topicoTitulo").value("Tópico de Estudo"))
                .andExpect(jsonPath("$.dadosDesenhoJson").value(json));
    }

    @Test
    @DisplayName("Deve salvar rascunho de canvas com status 200 OK via POST")
    void deveSalvarRascunhoViaPostComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        String json = "{\"paths\":[]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, json);
        RascunhoCanvasResponse response = new RascunhoCanvasResponse(
                canvasId, userId, topicoId, "Tópico de Estudo", json, Instant.now()
        );

        when(rascunhoCanvasService.salvarRascunho(eq(userId), eq(topicoId), any(SalvarRascunhoCanvasRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/canvas")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canvasId.toString()))
                .andExpect(jsonPath("$.topicoId").value(topicoId.toString()));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar salvar canvas sem dadosDesenhoJson")
    void deveRetornarBadRequestQuandoDadosDesenhoForemVazios() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, "   ");

        mockMvc.perform(put("/api/v1/canvas/topico/{topicoId}", topicoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve obter rascunho de canvas por topico com status 200 OK")
    void deveObterRascunhoPorTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        String json = "{\"paths\":[]}";
        RascunhoCanvasResponse response = new RascunhoCanvasResponse(
                canvasId, userId, topicoId, "Tópico", json, Instant.now()
        );

        when(rascunhoCanvasService.obterRascunhoPorTopico(userId, topicoId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/canvas/topico/{topicoId}", topicoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canvasId.toString()))
                .andExpect(jsonPath("$.topicoId").value(topicoId.toString()));
    }

    @Test
    @DisplayName("Deve obter rascunho de canvas por ID com status 200 OK")
    void deveObterRascunhoPorIdComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        String json = "{\"paths\":[]}";
        RascunhoCanvasResponse response = new RascunhoCanvasResponse(
                canvasId, userId, topicoId, "Tópico", json, Instant.now()
        );

        when(rascunhoCanvasService.obterRascunhoPorId(userId, canvasId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/canvas/{id}", canvasId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canvasId.toString()));
    }

    @Test
    @DisplayName("Deve listar rascunhos de canvas com status 200 OK")
    void deveListarRascunhosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        RascunhoCanvasResponse response = new RascunhoCanvasResponse(
                UUID.randomUUID(), userId, UUID.randomUUID(), "Tópico", "{}", Instant.now()
        );

        when(rascunhoCanvasService.listarRascunhos(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/canvas")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(response.id().toString()));
    }

    @Test
    @DisplayName("Deve apagar rascunho de canvas por topico com status 204 No Content")
    void deveApagarRascunhoPorTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        doNothing().when(rascunhoCanvasService).apagarRascunhoPorTopico(userId, topicoId);

        mockMvc.perform(delete("/api/v1/canvas/topico/{topicoId}", topicoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve apagar rascunho de canvas por ID com status 204 No Content")
    void deveApagarRascunhoPorIdComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();

        doNothing().when(rascunhoCanvasService).apagarRascunhoPorId(userId, canvasId);

        mockMvc.perform(delete("/api/v1/canvas/{id}", canvasId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized ao acessar endpoints sem JWT autenticado")
    void deveRetornarUnauthorizedSemJwt() throws Exception {
        mockMvc.perform(get("/api/v1/canvas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/canvas/topico/{topicoId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
