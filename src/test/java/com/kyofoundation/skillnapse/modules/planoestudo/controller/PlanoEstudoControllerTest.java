package com.kyofoundation.skillnapse.modules.planoestudo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.service.PlanoEstudoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanoEstudoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PlanoEstudoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PlanoEstudoService planoEstudoService;

    @Test
    @DisplayName("Deve criar plano de estudo e retornar 201 Created")
    void deveCriarPlanoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        PlanoEstudoRequest request = new PlanoEstudoRequest("Carreiras Jurídicas", "Magistratura e MP");
        PlanoEstudoResponse response = new PlanoEstudoResponse(planoId, "Carreiras Jurídicas", "Magistratura e MP", true, Instant.now(), Instant.now());

        when(planoEstudoService.criarPlano(any(PlanoEstudoRequest.class), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/plano-estudo")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(planoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Carreiras Jurídicas"));
    }

    @Test
    @DisplayName("Deve visualizar plano de estudo e retornar 200 OK")
    void deveVisualizarPlanoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        PlanoEstudoResponse response = new PlanoEstudoResponse(planoId, "Plano Fiscal", "Auditor", true, Instant.now(), Instant.now());

        when(planoEstudoService.visualizarPlano(eq(userId), eq(planoId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/plano-estudo/{planoEstudoId}", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Plano Fiscal"));
    }

    @Test
    @DisplayName("Deve editar plano de estudo e retornar 200 OK")
    void deveEditarPlanoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        EdicaoPlanoEstudoRequest request = new EdicaoPlanoEstudoRequest("Novo Titulo", "Nova Descricao", false);
        PlanoEstudoResponse response = new PlanoEstudoResponse(planoId, "Novo Titulo", "Nova Descricao", false, Instant.now(), Instant.now());

        when(planoEstudoService.editarPlano(eq(userId), eq(planoId), any(EdicaoPlanoEstudoRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/plano-estudo/{planoEstudoId}", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Novo Titulo"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @DisplayName("Deve apagar plano de estudo e retornar 204 No Content")
    void deveApagarPlanoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        doNothing().when(planoEstudoService).apagarPlano(userId, planoId);

        mockMvc.perform(delete("/api/v1/plano-estudo/{planoEstudoId}", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve listar planos de estudo paginados e retornar 200 OK")
    void deveListarPlanosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        PlanoEstudoResponse item = new PlanoEstudoResponse(UUID.randomUUID(), "Plano 1", "Desc", true, Instant.now(), Instant.now());
        PageImpl<PlanoEstudoResponse> page = new PageImpl<>(List.of(item));

        when(planoEstudoService.listarPlanos(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/plano-estudo")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Plano 1"));
    }
}
