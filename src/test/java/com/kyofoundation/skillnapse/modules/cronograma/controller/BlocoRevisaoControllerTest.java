package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AtualizarTopicosRevisaoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TopicoRevisaoItemResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;
import com.kyofoundation.skillnapse.modules.cronograma.service.BlocoRevisaoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlocoRevisaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class BlocoRevisaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private BlocoRevisaoService blocoRevisaoService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("[POST /template] Deve criar bloco de revisão no template com 201 Created")
    void deveCriarBlocoRevisaoTemplateComSucesso() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                templateId,
                DiaSemana.SEGUNDA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 30),
                null,
                List.of(topicoId)
        );

        BlocoRevisaoResponse response = new BlocoRevisaoResponse(
                UUID.randomUUID(),
                TipoOrigemBloco.TEMPLATE,
                templateId,
                DiaSemana.SEGUNDA,
                null,
                LocalTime.of(19, 0),
                LocalTime.of(20, 30),
                TipoBloco.REVISAO,
                null,
                null,
                List.of(new TopicoRevisaoItemResponse(topicoId, "Tópico Teste", null, null))
        );

        when(blocoRevisaoService.criarBlocoRevisaoTemplate(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/blocos-revisao/template")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.tipoBloco").value("REVISAO"))
                .andExpect(jsonPath("$.origem").value("TEMPLATE"))
                .andExpect(jsonPath("$.topicosRevisao[0].id").value(topicoId.toString()));
    }

    @Test
    @DisplayName("[POST /diario] Deve criar bloco de revisão avulso diário com 201 Created")
    void deveCriarBlocoRevisaoDiarioComSucesso() throws Exception {
        UUID topicoId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 10, 15);
        CriarBlocoRevisaoDiarioRequest request = new CriarBlocoRevisaoDiarioRequest(
                data,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                null,
                List.of(topicoId)
        );

        BlocoRevisaoResponse response = new BlocoRevisaoResponse(
                UUID.randomUUID(),
                TipoOrigemBloco.EXCECAO,
                null,
                null,
                data,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                TipoBloco.REVISAO,
                null,
                null,
                List.of(new TopicoRevisaoItemResponse(topicoId, "Tópico Teste", null, null))
        );

        when(blocoRevisaoService.criarBlocoRevisaoDiario(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/cronogramas/blocos-revisao/diario")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.origem").value("EXCECAO"))
                .andExpect(jsonPath("$.dataExcecao").value("2026-10-15"));
    }

    @Test
    @DisplayName("[PUT /template/{id}/topicos] Deve atualizar tópicos do bloco template com 200 OK")
    void deveAtualizarTopicosBlocoTemplateComSucesso() throws Exception {
        UUID blocoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        AtualizarTopicosRevisaoRequest request = new AtualizarTopicosRevisaoRequest(List.of(topicoId));

        BlocoRevisaoResponse response = new BlocoRevisaoResponse(
                blocoId,
                TipoOrigemBloco.TEMPLATE,
                UUID.randomUUID(),
                DiaSemana.QUARTA,
                null,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                TipoBloco.REVISAO,
                null,
                null,
                List.of(new TopicoRevisaoItemResponse(topicoId, "Novo Tópico", null, null))
        );

        when(blocoRevisaoService.atualizarTopicosBlocoTemplate(eq(userId), eq(blocoId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/cronogramas/blocos-revisao/template/{blocoId}/topicos", blocoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blocoId.toString()))
                .andExpect(jsonPath("$.topicosRevisao[0].titulo").value("Novo Tópico"));
    }

    @Test
    @DisplayName("[PUT /diario/{id}/topicos] Deve atualizar tópicos da exceção com 200 OK")
    void deveAtualizarTopicosBlocoDiarioComSucesso() throws Exception {
        UUID excecaoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        AtualizarTopicosRevisaoRequest request = new AtualizarTopicosRevisaoRequest(List.of(topicoId));

        BlocoRevisaoResponse response = new BlocoRevisaoResponse(
                excecaoId,
                TipoOrigemBloco.EXCECAO,
                null,
                null,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                TipoBloco.REVISAO,
                null,
                null,
                List.of(new TopicoRevisaoItemResponse(topicoId, "Tópico Atualizado", null, null))
        );

        when(blocoRevisaoService.atualizarTopicosBlocoDiario(eq(userId), eq(excecaoId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/cronogramas/blocos-revisao/diario/{excecaoId}/topicos", excecaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(excecaoId.toString()));
    }

    @Test
    @DisplayName("[GET /template/{id}/conteudo] Deve retornar conteúdo restrito de revisão com 200 OK")
    void deveObterConteudoRevisaoTemplateComSucesso() throws Exception {
        UUID blocoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        LocalDate dataRef = LocalDate.of(2026, 10, 10);

        ConteudoBlocoRevisaoResponse response = new ConteudoBlocoRevisaoResponse(
                blocoId,
                TipoOrigemBloco.TEMPLATE,
                dataRef,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                null,
                null,
                1,
                List.of(new TopicoRevisaoItemResponse(topicoId, "Direitos", null, null)),
                10L,
                0,
                List.of()
        );

        when(blocoRevisaoService.obterConteudoRevisaoTemplate(eq(userId), eq(blocoId), eq(dataRef))).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/blocos-revisao/template/{blocoId}/conteudo", blocoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .param("dataReferencia", "2026-10-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocoId").value(blocoId.toString()))
                .andExpect(jsonPath("$.totalCardsCadastrados").value(10))
                .andExpect(jsonPath("$.totalCardsParaRevisar").value(0));
    }

    @Test
    @DisplayName("[GET /diario/{id}/conteudo] Deve retornar conteúdo da exceção com 200 OK")
    void deveObterConteudoRevisaoDiarioComSucesso() throws Exception {
        UUID excecaoId = UUID.randomUUID();
        LocalDate dataRef = LocalDate.of(2026, 10, 15);

        ConteudoBlocoRevisaoResponse response = new ConteudoBlocoRevisaoResponse(
                excecaoId,
                TipoOrigemBloco.EXCECAO,
                dataRef,
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                null,
                null,
                0,
                List.of(),
                0L,
                0,
                List.of()
        );

        when(blocoRevisaoService.obterConteudoRevisaoDiario(eq(userId), eq(excecaoId), eq(dataRef))).thenReturn(response);

        mockMvc.perform(get("/api/v1/cronogramas/blocos-revisao/diario/{excecaoId}/conteudo", excecaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .param("dataReferencia", "2026-10-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocoId").value(excecaoId.toString()));
    }

    @Test
    @DisplayName("[POST /template] Deve retornar 400 Bad Request se campos obrigatórios ausentes")
    void deveRetornarBadRequestSeCamposAusentes() throws Exception {
        CriarBlocoRevisaoTemplateRequest requestInvalido = new CriarBlocoRevisaoTemplateRequest(
                null, null, null, null, null, List.of()
        );

        mockMvc.perform(post("/api/v1/cronogramas/blocos-revisao/template")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
