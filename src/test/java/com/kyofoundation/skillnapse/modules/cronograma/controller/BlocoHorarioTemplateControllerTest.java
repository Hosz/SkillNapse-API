package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoHorarioResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.service.BlocoHorarioTemplateService;
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

import java.time.LocalTime;
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

@WebMvcTest(BlocoHorarioTemplateController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class BlocoHorarioTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private BlocoHorarioTemplateService blocoHorarioTemplateService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[Adicionar bloco de horário] Deve adicionar bloco com 201 Created")
    void deveAdicionarBlocoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID blocoId = UUID.randomUUID();

        CriarBlocoHorarioRequest request = new CriarBlocoHorarioRequest(
                DiaSemana.SEGUNDA,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                TipoBloco.FOCO_TEORIA,
                null,
                null
        );

        BlocoHorarioResponse response = new BlocoHorarioResponse(
                blocoId, templateId, DiaSemana.SEGUNDA, LocalTime.of(14, 0), LocalTime.of(15, 30),
                TipoBloco.FOCO_TEORIA, null, null, null, null
        );

        when(blocoHorarioTemplateService.adicionarBlocoHorario(eq(userId), eq(templateId), any(CriarBlocoHorarioRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/templates/{templateId}/blocos", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(blocoId.toString()))
                .andExpect(jsonPath("$.diaSemana").value("SEGUNDA"));
    }

    @Test
    @DisplayName("[Listar blocos de horário do template] Deve listar blocos com 200 OK")
    void deveListarBlocosComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        BlocoHorarioResponse response = new BlocoHorarioResponse(
                UUID.randomUUID(), templateId, DiaSemana.TERCA, LocalTime.of(10, 0), LocalTime.of(11, 0),
                TipoBloco.REVISAO, null, null, null, null
        );

        when(blocoHorarioTemplateService.listarBlocosHorario(eq(userId), eq(templateId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/cronogramas/templates/{templateId}/blocos", templateId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].diaSemana").value("TERCA"));
    }

    @Test
    @DisplayName("[Visualizar bloco de horário] Deve visualizar bloco específico com 200 OK")
    void deveVisualizarBlocoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID blocoId = UUID.randomUUID();

        BlocoHorarioResponse response = new BlocoHorarioResponse(
                blocoId, templateId, DiaSemana.QUARTA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                TipoBloco.FOCO_TEORIA, null, null, null, null
        );

        when(blocoHorarioTemplateService.visualizarBlocoHorario(userId, templateId, blocoId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/templates/{templateId}/blocos/{blocoId}", templateId, blocoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blocoId.toString()));
    }

    @Test
    @DisplayName("[Editar bloco de horário] Deve atualizar bloco com 200 OK")
    void deveEditarBlocoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID blocoId = UUID.randomUUID();

        EditarBlocoHorarioRequest request = new EditarBlocoHorarioRequest(
                DiaSemana.QUINTA,
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                TipoBloco.SIMULADO,
                null,
                null
        );

        BlocoHorarioResponse response = new BlocoHorarioResponse(
                blocoId, templateId, DiaSemana.QUINTA, LocalTime.of(15, 0), LocalTime.of(16, 30),
                TipoBloco.SIMULADO, null, null, null, null
        );

        when(blocoHorarioTemplateService.editarBlocoHorario(eq(userId), eq(templateId), eq(blocoId), any(EditarBlocoHorarioRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/cronogramas/templates/{templateId}/blocos/{blocoId}", templateId, blocoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("QUINTA"))
                .andExpect(jsonPath("$.tipoBloco").value("SIMULADO"));
    }

    @Test
    @DisplayName("[Remover bloco de horário] Deve remover bloco com 204 No Content")
    void deveRemoverBlocoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID blocoId = UUID.randomUUID();

        doNothing().when(blocoHorarioTemplateService).apagarBlocoHorario(userId, templateId, blocoId);

        mockMvc.perform(delete("/api/v1/cronogramas/templates/{templateId}/blocos/{blocoId}", templateId, blocoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }
}
