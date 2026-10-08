package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.JanelaDisponibilidadeRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoSugeridoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.EstrategiaAgendamento;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.service.AutoAgendamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutoAgendamentoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AutoAgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AutoAgendamentoService autoAgendamentoService;

    @Test
    @DisplayName("[POST /sugestao] Deve simular auto-agendamento e retornar 200 OK")
    void deveGerarSugestaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(21, 0)
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                planoId, List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        BlocoSugeridoResponse bloco = new BlocoSugeridoResponse(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 0), TipoBloco.FOCO_TEORIA,
                UUID.randomUUID(), "Direito", null, null, "Justificativa"
        );
        SugestaoAutoAgendamentoResponse response = new SugestaoAutoAgendamentoResponse(
                planoId, "Concurso PF", 2.0, 1, "Resumo pedagógico", List.of(bloco)
        );

        when(autoAgendamentoService.gerarSugestao(eq(userId), any(GerarAutoAgendamentoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/auto-agendamento/sugestao")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planoEstudoId").value(planoId.toString()))
                .andExpect(jsonPath("$.totalBlocosSugeridos").value(1))
                .andExpect(jsonPath("$.blocosSugeridos[0].diaSemana").value("SEGUNDA"));
    }

    @Test
    @DisplayName("[POST /aplicar] Deve gerar e persistir agendamento retornando 201 Created")
    void deveAplicarAgendamentoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest bloco =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest(
                        DiaSemana.SABADO, LocalTime.of(8, 0), LocalTime.of(10, 0), TipoBloco.FOCO_TEORIA, null, null, "Estudo"
                );
        AplicarAutoAgendamentoRequest request = new AplicarAutoAgendamentoRequest(
                planoId, null, "Template IA", true, "Resumo", List.of(bloco)
        );

        ResultadoAutoAgendamentoResponse response = new ResultadoAutoAgendamentoResponse(
                templateId, "Template IA", 2, "Resumo", Collections.emptyList()
        );

        when(autoAgendamentoService.aplicarAgendamento(eq(userId), any(AplicarAutoAgendamentoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/auto-agendamento/aplicar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateSemanalId").value(templateId.toString()))
                .andExpect(jsonPath("$.nomeTemplate").value("Template IA"))
                .andExpect(jsonPath("$.totalBlocosPersistidos").value(2));
    }

    @Test
    @DisplayName("[POST /sugestao] Deve retornar 400 Bad Request quando corpo for invalido")
    void deveRetornarBadRequestQuandoCorpoInvalido() throws Exception {
        UUID userId = UUID.randomUUID();
        GerarAutoAgendamentoRequest requestInvalido = new GerarAutoAgendamentoRequest(
                null, Collections.emptyList(), 10, -5, true, true, null // duracao 10 < 30, intervalo -5 < 0, disponibilidades vazias
        );

        mockMvc.perform(post("/api/v1/cronogramas/auto-agendamento/sugestao")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
