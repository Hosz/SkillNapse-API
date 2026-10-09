package com.kyofoundation.skillnapse.modules.questao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.questao.dto.request.GerarSimuladoAdaptativoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.AlternativaItemSimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoItemSimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoAdaptativoResponse;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.enums.TipoSimulado;
import com.kyofoundation.skillnapse.modules.questao.service.SimuladoAdaptativoService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimuladoAdaptativoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SimuladoAdaptativoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private SimuladoAdaptativoService simuladoAdaptativoService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve gerar simulado adaptativo com status 201 Created")
    void deveGerarSimuladoAdaptativoComSucesso() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        UUID simId = UUID.randomUUID();
        UUID questaoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                planoId, 5, "FGV", List.of(topicoId)
        );

        SimuladoAdaptativoResponse response = new SimuladoAdaptativoResponse(
                simId,
                "Simulado Adaptativo IA - FGV",
                TipoSimulado.ADAPTATIVO_IA,
                false,
                Instant.now(),
                5,
                List.of(new QuestaoItemSimuladoResponse(
                        questaoId,
                        topicoId,
                        "Direito Processual",
                        "Mandado de Segurança",
                        "Qual o prazo para impetrar mandado de segurança?",
                        DificuldadeQuestao.MEDIA,
                        "FGV",
                        2024,
                        List.of(new AlternativaItemSimuladoResponse(UUID.randomUUID(), "A", "120 dias"))
                ))
        );

        when(simuladoAdaptativoService.gerarSimuladoAdaptativo(eq(userId), any(GerarSimuladoAdaptativoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.simuladoId").value(simId.toString()))
                .andExpect(jsonPath("$.titulo").value("Simulado Adaptativo IA - FGV"))
                .andExpect(jsonPath("$.tipo").value("ADAPTATIVO_IA"))
                .andExpect(jsonPath("$.concluido").value(false))
                .andExpect(jsonPath("$.totalQuestoes").value(5))
                .andExpect(jsonPath("$.questoes[0].id").value(questaoId.toString()))
                .andExpect(jsonPath("$.questoes[0].enunciado").value("Qual o prazo para impetrar mandado de segurança?"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando planoEstudoId for nulo")
    void deveRetornar400QuandoPlanoEstudoIdNulo() throws Exception {
        UUID userId = UUID.randomUUID();
        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                null, 5, "FGV", null
        );

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando não houver autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        UUID planoId = UUID.randomUUID();
        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                planoId, 5, "FGV", null
        );

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando usuário não for dono do plano")
    void deveRetornar403QuandoUsuarioNaoDonoDoPlano() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                planoId, 5, "FGV", null
        );

        when(simuladoAdaptativoService.gerarSimuladoAdaptativo(eq(userId), any(GerarSimuladoAdaptativoRequest.class)))
                .thenThrow(new ForbiddenException("O usuário não tem permissão para acessar este plano de estudo."));

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("O usuário não tem permissão para acessar este plano de estudo."));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando plano não for encontrado")
    void deveRetornar404QuandoPlanoNaoEncontrado() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                planoId, 5, "FGV", null
        );

        when(simuladoAdaptativoService.gerarSimuladoAdaptativo(eq(userId), any(GerarSimuladoAdaptativoRequest.class)))
                .thenThrow(new ResourceNotFoundException("Plano de estudo não encontrado com o id: " + planoId));

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plano de estudo não encontrado com o id: " + planoId));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando serviço lançar BadRequestException")
    void deveRetornar400QuandoServicoLancarBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        GerarSimuladoAdaptativoRequest request = new GerarSimuladoAdaptativoRequest(
                planoId, 5, "FGV", null
        );

        when(simuladoAdaptativoService.gerarSimuladoAdaptativo(eq(userId), any(GerarSimuladoAdaptativoRequest.class)))
                .thenThrow(new BadRequestException("Nenhum tópico válido foi encontrado para geração do simulado adaptativo."));

        mockMvc.perform(post("/api/v1/simulados/adaptativo/gerar")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nenhum tópico válido foi encontrado para geração do simulado adaptativo."));
    }
}
