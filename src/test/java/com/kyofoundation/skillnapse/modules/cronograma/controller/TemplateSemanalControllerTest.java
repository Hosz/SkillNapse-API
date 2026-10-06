package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.GradeSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.service.TemplateSemanalService;
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
import java.util.Collections;
import java.util.EnumMap;
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

@WebMvcTest(TemplateSemanalController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TemplateSemanalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TemplateSemanalService templateSemanalService;

    @Test
    @DisplayName("[Criar template semanal] Deve criar template e retornar 201 Created")
    void deveCriarTemplateComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        CriarTemplateSemanalRequest request = new CriarTemplateSemanalRequest("Rotina Padrão 2026", true);
        TemplateSemanalResponse response = new TemplateSemanalResponse(templateId, "Rotina Padrão 2026", true, Instant.now(), 0);

        when(templateSemanalService.criarTemplate(eq(userId), any(CriarTemplateSemanalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/templates")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(templateId.toString()))
                .andExpect(jsonPath("$.nome").value("Rotina Padrão 2026"));
    }

    @Test
    @DisplayName("[Listar templates semanais] Deve listar templates e retornar 200 OK")
    void deveListarTemplates() throws Exception {
        UUID userId = UUID.randomUUID();
        TemplateSemanalResponse response = new TemplateSemanalResponse(UUID.randomUUID(), "Rotina", true, Instant.now(), 0);

        when(templateSemanalService.listarTemplates(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/cronogramas/templates")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Rotina"));
    }

    @Test
    @DisplayName("[Visualizar template ativo] Deve retornar 200 OK com template ativo")
    void deveVisualizarTemplateAtivo() throws Exception {
        UUID userId = UUID.randomUUID();
        TemplateSemanalResponse response = new TemplateSemanalResponse(UUID.randomUUID(), "Template Ativo", true, Instant.now(), 2);

        when(templateSemanalService.visualizarTemplateAtivo(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/templates/ativo")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Template Ativo"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("[Visualizar template semanal] Deve retornar detalhes do template com 200 OK")
    void deveVisualizarTemplate() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        TemplateSemanalResponse response = new TemplateSemanalResponse(templateId, "Template 1", true, Instant.now(), 0);

        when(templateSemanalService.visualizarTemplate(userId, templateId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/templates/{templateId}", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(templateId.toString()));
    }

    @Test
    @DisplayName("[Visualizar grade semanal completa] Deve retornar a matriz semanal agrupada")
    void deveVisualizarGradeSemanal() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        GradeSemanalResponse gradeResponse = new GradeSemanalResponse(
                templateId, "Matriz", true, new EnumMap<>(DiaSemana.class));

        when(templateSemanalService.visualizarGradeSemanal(userId, templateId)).thenReturn(gradeResponse);

        mockMvc.perform(get("/api/v1/cronogramas/templates/{templateId}/grade", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value(templateId.toString()));
    }

    @Test
    @DisplayName("[Editar template semanal] Deve atualizar template com 200 OK")
    void deveEditarTemplate() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        EditarTemplateSemanalRequest request = new EditarTemplateSemanalRequest("Novo Nome", true);
        TemplateSemanalResponse response = new TemplateSemanalResponse(templateId, "Novo Nome", true, Instant.now(), 0);

        when(templateSemanalService.editarTemplate(eq(userId), eq(templateId), any(EditarTemplateSemanalRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/cronogramas/templates/{templateId}", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    @Test
    @DisplayName("[Ativar template semanal] Deve ativar template com 200 OK")
    void deveAtivarTemplate() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        TemplateSemanalResponse response = new TemplateSemanalResponse(templateId, "Template", true, Instant.now(), 0);

        when(templateSemanalService.ativarTemplate(userId, templateId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cronogramas/templates/{templateId}/ativar", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("[Apagar template semanal] Deve remover template com 204 No Content")
    void deveApagarTemplate() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        doNothing().when(templateSemanalService).apagarTemplate(userId, templateId);

        mockMvc.perform(delete("/api/v1/cronogramas/templates/{templateId}", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }
}
