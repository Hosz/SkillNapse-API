package com.kyofoundation.skillnapse.modules.questao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.ResponderQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.AlternativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.HistoricoTentativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoDetalheResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoResumoResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.ResultadoResolucaoResponse;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.service.QuestaoService;
import com.kyofoundation.skillnapse.modules.questao.service.ResolucaoQuestaoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestaoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestaoService questaoService;

    @MockitoBean
    private ResolucaoQuestaoService resolucaoQuestaoService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve cadastrar nova questão com status 201 Created")
    void deveCadastrarQuestaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();

        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Direito Administrativo",
                "Licitações",
                "A dispensa de licitação é taxativa?",
                "Art. 75 da Lei 14.133.",
                DificuldadeQuestao.MEDIA,
                "FGV",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "Sim", true),
                        new CriarAlternativaRequest("B", "Não", false)
                )
        );

        QuestaoDetalheResponse response = new QuestaoDetalheResponse(
                questaoId,
                "Direito Administrativo",
                "Licitações",
                "A dispensa de licitação é taxativa?",
                "Art. 75 da Lei 14.133.",
                DificuldadeQuestao.MEDIA,
                "FGV",
                2024,
                false,
                Instant.now(),
                List.of(
                        new AlternativaResponse(UUID.randomUUID(), "A", "Sim", true),
                        new AlternativaResponse(UUID.randomUUID(), "B", "Não", false)
                )
        );

        when(questaoService.criar(any(CriarQuestaoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/questoes")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(questaoId.toString()))
                .andExpect(jsonPath("$.assuntoGeral").value("Direito Administrativo"))
                .andExpect(jsonPath("$.alternativas").isArray());
    }

    @Test
    @DisplayName("Deve buscar questões com filtros com status 200 OK")
    void deveBuscarQuestoesComFiltros() throws Exception {
        UUID userId = UUID.randomUUID();
        QuestaoResumoResponse resumo = new QuestaoResumoResponse(
                UUID.randomUUID(),
                "Direito Penal",
                "Crimes",
                "O homicídio culposo...",
                DificuldadeQuestao.FACIL,
                "VUNESP",
                2023,
                Instant.now(),
                4
        );

        when(questaoService.buscarComFiltros(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resumo)));

        mockMvc.perform(get("/api/v1/questoes")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .param("assuntoGeral", "Direito Penal")
                        .param("dificuldade", "FACIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assuntoGeral").value("Direito Penal"));
    }

    @Test
    @DisplayName("Deve buscar questão por ID com status 200 OK")
    void deveBuscarQuestaoPorId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();

        QuestaoDetalheResponse detalhe = new QuestaoDetalheResponse(
                questaoId,
                "Direito Constitucional",
                "Poder Judiciário",
                "Enunciado completo",
                "Explicação gabarito",
                DificuldadeQuestao.MEDIA,
                "CESPE",
                2024,
                false,
                Instant.now(),
                List.of()
        );

        when(questaoService.buscarPorId(questaoId)).thenReturn(detalhe);

        mockMvc.perform(get("/api/v1/questoes/{id}", questaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questaoId.toString()))
                .andExpect(jsonPath("$.topicoReferencia").value("Poder Judiciário"));
    }

    @Test
    @DisplayName("Deve responder questão com status 200 OK")
    void deveResponderQuestaoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();
        UUID altId = UUID.randomUUID();

        ResponderQuestaoRequest request = new ResponderQuestaoRequest(altId, 45, null, null);
        ResultadoResolucaoResponse resultado = new ResultadoResolucaoResponse(
                UUID.randomUUID(),
                questaoId,
                altId,
                "A",
                true,
                altId,
                "A",
                "Gabarito oficial",
                45,
                Instant.now()
        );

        when(resolucaoQuestaoService.responder(eq(questaoId), any(ResponderQuestaoRequest.class), eq(userId)))
                .thenReturn(resultado);

        mockMvc.perform(post("/api/v1/questoes/{id}/responder", questaoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertou").value(true))
                .andExpect(jsonPath("$.letraEscolhida").value("A"))
                .andExpect(jsonPath("$.explicacaoGabarito").value("Gabarito oficial"));
    }

    @Test
    @DisplayName("Deve listar histórico de tentativas com status 200 OK")
    void deveListarHistoricoTentativas() throws Exception {
        UUID userId = UUID.randomUUID();
        HistoricoTentativaResponse historico = new HistoricoTentativaResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Enunciado",
                "Português",
                "Sintaxe",
                UUID.randomUUID(),
                "B",
                false,
                30,
                null,
                Instant.now()
        );

        when(resolucaoQuestaoService.buscarHistorico(eq(userId), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(historico)));

        mockMvc.perform(get("/api/v1/questoes/historico")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assuntoGeral").value("Português"))
                .andExpect(jsonPath("$.content[0].acertou").value(false));
    }
}
