package com.kyofoundation.skillnapse.modules.planoestudo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.AtualizarProgressoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.TopicoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import com.kyofoundation.skillnapse.modules.planoestudo.service.TopicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(TopicoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TopicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TopicoService topicoService;

    @Test
    @DisplayName("Deve criar tópico e retornar 201 Created")
    void deveCriarTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        CriarTopicoRequest request = new CriarTopicoRequest("Remédios Constitucionais", null, 3, NivelProficiencia.INICIANTE, 1);
        TopicoResponse response = new TopicoResponse(topicoId, materiaId, null, "Remédios Constitucionais", NivelProficiencia.INICIANTE, 3, false, 1, Instant.now(), Instant.now());

        when(topicoService.criarTopico(eq(userId), eq(materiaId), any(CriarTopicoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/topicos/materia/{materiaId}", materiaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(topicoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Remédios Constitucionais"))
                .andExpect(jsonPath("$.pesoEdital").value(3));
    }

    @Test
    @DisplayName("Deve visualizar tópico por ID e retornar 200 OK")
    void deveVisualizarTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        TopicoResponse response = new TopicoResponse(topicoId, UUID.randomUUID(), null, "Mandado de Segurança", NivelProficiencia.INTERMEDIARIO, 2, true, 0, Instant.now(), Instant.now());

        when(topicoService.visualizarTopico(eq(userId), eq(topicoId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/topicos/{topicoId}", topicoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicoId.toString()))
                .andExpect(jsonPath("$.titulo").value("Mandado de Segurança"));
    }

    @Test
    @DisplayName("Deve editar tópico e retornar 200 OK")
    void deveEditarTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        EditarTopicoRequest request = new EditarTopicoRequest("Habeas Corpus", 4, NivelProficiencia.AVANCADO, true, 2);
        TopicoResponse response = new TopicoResponse(topicoId, UUID.randomUUID(), null, "Habeas Corpus", NivelProficiencia.AVANCADO, 4, true, 2, Instant.now(), Instant.now());

        when(topicoService.editarTopico(eq(userId), eq(topicoId), any(EditarTopicoRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/topicos/{topicoId}", topicoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Habeas Corpus"))
                .andExpect(jsonPath("$.pesoEdital").value(4));
    }

    @Test
    @DisplayName("Deve atualizar progresso e proficiência do tópico e retornar 200 OK")
    void deveAtualizarProgressoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        AtualizarProgressoRequest request = new AtualizarProgressoRequest(true, NivelProficiencia.AVANCADO);
        TopicoResponse response = new TopicoResponse(topicoId, UUID.randomUUID(), null, "Ação Popular", NivelProficiencia.AVANCADO, 1, true, 0, Instant.now(), Instant.now());

        when(topicoService.atualizarStatusEProficiencia(eq(userId), eq(topicoId), any(AtualizarProgressoRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/topicos/{topicoId}/progresso", topicoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluido").value(true))
                .andExpect(jsonPath("$.nivelProficiencia").value("AVANCADO"));
    }

    @Test
    @DisplayName("Deve deletar tópico e retornar 204 No Content")
    void deveDeletarTopicoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        doNothing().when(topicoService).deletarTopico(userId, topicoId);

        mockMvc.perform(delete("/api/v1/topicos/{topicoId}", topicoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve listar árvore de tópicos da matéria e retornar 200 OK")
    void deveListarArvoreTopicosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        TopicoResponse item = new TopicoResponse(UUID.randomUUID(), materiaId, null, "Controle de Constitucionalidade", NivelProficiencia.AVANCADO, 5, false, 0, Instant.now(), Instant.now());

        when(topicoService.listarArvoreTopicosPorMateria(eq(userId), eq(materiaId))).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/topicos/materia/{materiaId}", materiaId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Controle de Constitucionalidade"));
    }
}
