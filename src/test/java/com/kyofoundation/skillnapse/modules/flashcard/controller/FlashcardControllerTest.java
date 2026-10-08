package com.kyofoundation.skillnapse.modules.flashcard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.RevisarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.FlashcardResponse;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.HistoricoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import com.kyofoundation.skillnapse.modules.flashcard.service.FlashcardService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(FlashcardController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private FlashcardService flashcardService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve criar flashcard com status 201 Created")
    void deveCriarFlashcardComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        CriarFlashcardRequest request = new CriarFlashcardRequest(baralhoId, null, "Frente", "Verso");
        FlashcardResponse response = new FlashcardResponse(
                cardId, baralhoId, "Baralho", null, null, "Frente", "Verso",
                new BigDecimal("2.50"), 0, 0, LocalDate.now(), null
        );

        when(flashcardService.criarFlashcard(eq(userId), any(CriarFlashcardRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/flashcards")
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.frente").value("Frente"));
    }

    @Test
    @DisplayName("Deve obter flashcard por ID com status 200 OK")
    void deveObterFlashcardPorId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        FlashcardResponse response = new FlashcardResponse(
                cardId, UUID.randomUUID(), "Baralho", null, null, "Frente", "Verso",
                new BigDecimal("2.50"), 0, 0, LocalDate.now(), null
        );

        when(flashcardService.obterFlashcard(userId, cardId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/flashcards/{id}", cardId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()));
    }

    @Test
    @DisplayName("Deve atualizar flashcard com status 200 OK")
    void deveAtualizarFlashcard() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        AtualizarFlashcardRequest request = new AtualizarFlashcardRequest(null, "Nova Frente", "Novo Verso");
        FlashcardResponse response = new FlashcardResponse(
                cardId, UUID.randomUUID(), "Baralho", null, null, "Nova Frente", "Novo Verso",
                new BigDecimal("2.50"), 0, 0, LocalDate.now(), null
        );

        when(flashcardService.atualizarFlashcard(eq(userId), eq(cardId), any(AtualizarFlashcardRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/flashcards/{id}", cardId)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frente").value("Nova Frente"));
    }

    @Test
    @DisplayName("Deve revisar flashcard com status 200 OK")
    void deveRevisarFlashcard() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        RevisarFlashcardRequest request = new RevisarFlashcardRequest(ClassificacaoResposta.BOM, 6);
        FlashcardResponse response = new FlashcardResponse(
                cardId, UUID.randomUUID(), "Baralho", null, null, "Frente", "Verso",
                new BigDecimal("2.50"), 6, 2, LocalDate.now().plusDays(6), Instant.now()
        );

        when(flashcardService.revisarFlashcard(eq(userId), eq(cardId), any(RevisarFlashcardRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/flashcards/{id}/revisar", cardId)
                        .with(jwt().jwt(j -> j.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervaloDias").value(6))
                .andExpect(jsonPath("$.repeticoes").value(2));
    }

    @Test
    @DisplayName("Deve listar cards vencidos (/due) com status 200 OK")
    void deveListarCardsVencidos() throws Exception {
        UUID userId = UUID.randomUUID();
        FlashcardResponse response = new FlashcardResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Baralho", null, null, "Frente", "Verso",
                new BigDecimal("2.50"), 0, 0, LocalDate.now(), null
        );

        when(flashcardService.listarCardsVencidos(eq(userId), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/flashcards/due")
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].frente").value("Frente"));
    }

    @Test
    @DisplayName("Deve listar cards por baralho com status 200 OK")
    void deveListarCardsPorBaralho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        FlashcardResponse response = new FlashcardResponse(
                UUID.randomUUID(), baralhoId, "Baralho", null, null, "Frente", "Verso",
                new BigDecimal("2.50"), 0, 0, LocalDate.now(), null
        );

        when(flashcardService.listarFlashcardsPorBaralho(eq(userId), eq(baralhoId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/flashcards/baralho/{baralhoId}", baralhoId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].baralhoId").value(baralhoId.toString()));
    }

    @Test
    @DisplayName("Deve listar historico de revisoes com status 200 OK")
    void deveListarHistoricoRevisoes() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        HistoricoRevisaoResponse hist = new HistoricoRevisaoResponse(
                UUID.randomUUID(), cardId, ClassificacaoResposta.FACIL, 4, Instant.now()
        );

        when(flashcardService.listarHistoricoRevisoes(eq(userId), eq(cardId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hist)));

        mockMvc.perform(get("/api/v1/flashcards/{id}/historico", cardId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].classificacaoResposta").value("FACIL"));
    }

    @Test
    @DisplayName("Deve apagar flashcard com status 204 No Content")
    void deveApagarFlashcard() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        doNothing().when(flashcardService).apagarFlashcard(userId, cardId);

        mockMvc.perform(delete("/api/v1/flashcards/{id}", cardId)
                        .with(jwt().jwt(j -> j.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para requests sem JWT")
    void deveRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/flashcards/due"))
                .andExpect(status().isUnauthorized());
    }
}
