package com.kyofoundation.skillnapse.modules.planoestudo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.MateriaResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.service.MateriaService;
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

@WebMvcTest(MateriaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MateriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MateriaService materiaService;

    @Test
    @DisplayName("Deve criar matéria vinculada ao plano e retornar 201 Created")
    void deveCriarMateriaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        CriarMateriaRequest request = new CriarMateriaRequest("Direito Constitucional", "#3B82F6", 1);
        MateriaResponse response = new MateriaResponse(materiaId, "Direito Constitucional", "#3B82F6", 1, Instant.now());

        when(materiaService.criarMateria(eq(userId), eq(planoId), any(CriarMateriaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/materias/plano/{planoId}", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(materiaId.toString()))
                .andExpect(jsonPath("$.nome").value("Direito Constitucional"))
                .andExpect(jsonPath("$.corHex").value("#3B82F6"));
    }

    @Test
    @DisplayName("Deve visualizar matéria por ID e retornar 200 OK")
    void deveVisualizarMateriaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        MateriaResponse response = new MateriaResponse(materiaId, "Direito Administrativo", "#10B981", 2, Instant.now());

        when(materiaService.verMateria(eq(userId), eq(materiaId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/materias/{materiaId}", materiaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(materiaId.toString()))
                .andExpect(jsonPath("$.nome").value("Direito Administrativo"));
    }

    @Test
    @DisplayName("Deve editar matéria e retornar 200 OK")
    void deveEditarMateriaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        EditarMateriaRequest request = new EditarMateriaRequest("Direito Administrativo Avançado", "#059669", 3);
        MateriaResponse response = new MateriaResponse(materiaId, "Direito Administrativo Avançado", "#059669", 3, Instant.now());

        when(materiaService.editarMateria(eq(userId), eq(materiaId), any(EditarMateriaRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/materias/{materiaId}", materiaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Direito Administrativo Avançado"))
                .andExpect(jsonPath("$.corHex").value("#059669"));
    }

    @Test
    @DisplayName("Deve apagar matéria e retornar 204 No Content")
    void deveApagarMateriaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        doNothing().when(materiaService).apagarMateria(userId, materiaId);

        mockMvc.perform(delete("/api/v1/materias/{materiaId}", materiaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve listar matérias de um plano paginadas e retornar 200 OK")
    void deveListarMateriasComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        MateriaResponse item = new MateriaResponse(UUID.randomUUID(), "Materia 1", "#FFFFFF", 0, Instant.now());
        PageImpl<MateriaResponse> page = new PageImpl<>(List.of(item));

        when(materiaService.listarMaterias(eq(userId), eq(planoId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/materias/plano/{planoId}", planoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Materia 1"));
    }
}
