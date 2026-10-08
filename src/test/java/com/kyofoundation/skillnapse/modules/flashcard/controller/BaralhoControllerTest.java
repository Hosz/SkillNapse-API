package com.kyofoundation.skillnapse.modules.flashcard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.service.BaralhoService;
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

@WebMvcTest(BaralhoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class BaralhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private BaralhoService baralhoService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve criar baralho com status 201 Created")
    void deveCriarBaralhoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        CriarBaralhoRequest request = new CriarBaralhoRequest("Constitucional", "Descrição", null);
        BaralhoResponse response = new BaralhoResponse(
                baralhoId, userId, null, null, "Constitucional", "Descrição", Instant.now(), 0L, 0L
        );

        when(baralhoService.criarBaralho(eq(userId), any(CriarBaralhoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/baralhos")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(baralhoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Constitucional"));
    }

    @Test
    @DisplayName("Deve listar baralhos com status 200 OK")
    void deveListarBaralhosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        BaralhoResponse response = new BaralhoResponse(
                UUID.randomUUID(), userId, null, null, "Constitucional", null, Instant.now(), 5L, 2L
        );

        when(baralhoService.listarBaralhos(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/baralhos")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Constitucional"));
    }

    @Test
    @DisplayName("Deve obter baralho por ID com status 200 OK")
    void deveObterBaralhoPorId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        BaralhoResponse response = new BaralhoResponse(
                baralhoId, userId, null, null, "Constitucional", null, Instant.now(), 5L, 2L
        );

        when(baralhoService.obterBaralho(userId, baralhoId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/baralhos/{id}", baralhoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(baralhoId.toString()));
    }

    @Test
    @DisplayName("Deve atualizar baralho com status 200 OK")
    void deveAtualizarBaralho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        AtualizarBaralhoRequest request = new AtualizarBaralhoRequest("Novo Título", null, null);
        BaralhoResponse response = new BaralhoResponse(
                baralhoId, userId, null, null, "Novo Título", null, Instant.now(), 5L, 2L
        );

        when(baralhoService.atualizarBaralho(eq(userId), eq(baralhoId), any(AtualizarBaralhoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/baralhos/{id}", baralhoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo Título"));
    }

    @Test
    @DisplayName("Deve apagar baralho com status 204 No Content")
    void deveApagarBaralho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();

        doNothing().when(baralhoService).apagarBaralho(userId, baralhoId);

        mockMvc.perform(delete("/api/v1/baralhos/{id}", baralhoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized sem token JWT")
    void deveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/baralhos"))
                .andExpect(status().isUnauthorized());
    }
}
