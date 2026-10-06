package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.RegistrarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.AgendaDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ExcecaoDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.service.CronogramaDiarioService;
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

@WebMvcTest(CronogramaDiarioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CronogramaDiarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private CronogramaDiarioService cronogramaDiarioService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[Registrar exceção diária] Deve registrar excecao e retornar 201 Created")
    void deveRegistrarExcecaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 10, 15);
        UUID excecaoId = UUID.randomUUID();

        RegistrarExcecaoDiariaRequest request = new RegistrarExcecaoDiariaRequest(
                TipoAcaoExcecao.BLOCO_AVULSO,
                null,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                TipoBloco.SIMULADO,
                null,
                null
        );

        ExcecaoDiariaResponse response = new ExcecaoDiariaResponse(
                excecaoId, data, TipoAcaoExcecao.BLOCO_AVULSO, null, null,
                LocalTime.of(19, 0), LocalTime.of(20, 0), TipoBloco.SIMULADO, null, null, null, null
        );

        when(cronogramaDiarioService.registrarExcecao(eq(userId), eq(data), any(RegistrarExcecaoDiariaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/diario/{data}/excecoes", data)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(excecaoId.toString()))
                .andExpect(jsonPath("$.tipoAcao").value("BLOCO_AVULSO"));
    }

    @Test
    @DisplayName("[Editar exceção diária] Deve editar excecao e retornar 200 OK")
    void deveEditarExcecaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID excecaoId = UUID.randomUUID();

        EditarExcecaoDiariaRequest request = new EditarExcecaoDiariaRequest(
                null,
                null,
                LocalTime.of(18, 0),
                LocalTime.of(19, 30),
                TipoBloco.REVISAO,
                null,
                null
        );

        ExcecaoDiariaResponse response = new ExcecaoDiariaResponse(
                excecaoId, LocalDate.of(2026, 10, 15), TipoAcaoExcecao.BLOCO_AVULSO, null, null,
                LocalTime.of(18, 0), LocalTime.of(19, 30), TipoBloco.REVISAO, null, null, null, null
        );

        when(cronogramaDiarioService.editarExcecaoDiaria(eq(userId), eq(excecaoId), any(EditarExcecaoDiariaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/cronogramas/diario/excecoes/{excecaoId}", excecaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoBloco").value("REVISAO"));
    }

    @Test
    @DisplayName("[Remover exceção pontual] Deve remover excecao e retornar 204 No Content")
    void deveRemoverExcecaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID excecaoId = UUID.randomUUID();

        doNothing().when(cronogramaDiarioService).removerExcecaoDiaria(userId, excecaoId);

        mockMvc.perform(delete("/api/v1/cronogramas/diario/excecoes/{excecaoId}", excecaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[Resetar agenda do dia] Deve resetar o dia e retornar 204 No Content")
    void deveResetarAgendaDiaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 10, 15);

        doNothing().when(cronogramaDiarioService).resetarAgendaDia(userId, data);

        mockMvc.perform(delete("/api/v1/cronogramas/diario/{data}/resetar", data)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[Visualizar agenda do dia] Deve projetar o dia e retornar 200 OK com agenda completa")
    void deveVisualizarAgendaDiaComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 10, 15);
        UUID templateId = UUID.randomUUID();

        AgendaDiariaResponse response = new AgendaDiariaResponse(
                data, DiaSemana.QUINTA, templateId, "Padrão", List.of()
        );

        when(cronogramaDiarioService.visualizarAgendaDia(userId, data)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/diario/{data}", data)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2026-10-15"))
                .andExpect(jsonPath("$.diaSemana").value("QUINTA"));
    }
}
