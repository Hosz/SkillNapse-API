package com.kyofoundation.skillnapse.modules.edital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.edital.dto.request.AtualizarRascunhoEditalRequest;
import com.kyofoundation.skillnapse.modules.edital.dto.request.ConverterRascunhoEditalRequest;
import com.kyofoundation.skillnapse.modules.edital.dto.response.ConversaoEditalResponse;
import com.kyofoundation.skillnapse.modules.edital.dto.response.RascunhoEditalResponse;
import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalArvoreEstruturada;
import com.kyofoundation.skillnapse.modules.edital.enums.StatusRascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.service.EditalParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EditalParserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class EditalParserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private EditalParserService editalParserService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve fazer upload de edital com status 201 Created")
    void deveFazerUploadDeEdital() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "edital.pdf", "application/pdf", "conteudo".getBytes()
        );

        EditalArvoreEstruturada arvore = new EditalArvoreEstruturada("Concurso BB", "Escriturário", List.of());
        RascunhoEditalResponse response = new RascunhoEditalResponse(
                UUID.randomUUID(), "edital.pdf", null, StatusRascunhoEdital.AGUARDANDO_APROVACAO, arvore, Instant.now()
        );

        when(editalParserService.uploadEditalPdf(eq(userId), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/editais/parse")
                        .file(arquivo)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeArquivo").value("edital.pdf"))
                .andExpect(jsonPath("$.conteudo.nomeConcurso").value("Concurso BB"));
    }

    @Test
    @DisplayName("Deve fazer upload de edital especificando cargo alvo com sucesso")
    void deveFazerUploadDeEditalComCargoAlvo() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "edital_pf.pdf", "application/pdf", "%PDF-1.4 mock".getBytes()
        );

        EditalArvoreEstruturada arvore = new EditalArvoreEstruturada("Polícia Federal", "Agente", List.of());
        RascunhoEditalResponse response = new RascunhoEditalResponse(
                UUID.randomUUID(), "edital_pf.pdf", null, StatusRascunhoEdital.AGUARDANDO_APROVACAO, arvore, Instant.now()
        );

        when(editalParserService.uploadEditalPdf(eq(userId), any(), eq("Agente de Polícia"))).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/editais/parse")
                        .file(arquivo)
                        .param("cargoAlvo", "Agente de Polícia")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeArquivo").value("edital_pf.pdf"))
                .andExpect(jsonPath("$.conteudo.nomeConcurso").value("Polícia Federal"));
    }

    @Test
    @DisplayName("Deve listar rascunhos com status 200 OK")
    void deveListarRascunhos() throws Exception {
        UUID userId = UUID.randomUUID();
        RascunhoEditalResponse response = new RascunhoEditalResponse(
                UUID.randomUUID(), "edital.pdf", null, StatusRascunhoEdital.AGUARDANDO_APROVACAO, null, Instant.now()
        );

        when(editalParserService.listarRascunhos(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/editais/rascunhos")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeArquivo").value("edital.pdf"));
    }

    @Test
    @DisplayName("Deve visualizar rascunho por id com status 200 OK")
    void deveVisualizarRascunho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rascunhoId = UUID.randomUUID();
        RascunhoEditalResponse response = new RascunhoEditalResponse(
                rascunhoId, "edital.pdf", null, StatusRascunhoEdital.AGUARDANDO_APROVACAO, null, Instant.now()
        );

        when(editalParserService.visualizarRascunho(userId, rascunhoId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/editais/rascunhos/{rascunhoId}", rascunhoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rascunhoId.toString()));
    }

    @Test
    @DisplayName("Deve atualizar rascunho com status 200 OK")
    void deveAtualizarRascunho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rascunhoId = UUID.randomUUID();
        EditalArvoreEstruturada arvore = new EditalArvoreEstruturada("Concurso Novo", "Cargo", List.of());
        AtualizarRascunhoEditalRequest request = new AtualizarRascunhoEditalRequest(arvore);

        RascunhoEditalResponse response = new RascunhoEditalResponse(
                rascunhoId, "edital.pdf", null, StatusRascunhoEdital.AGUARDANDO_APROVACAO, arvore, Instant.now()
        );

        when(editalParserService.atualizarRascunho(eq(userId), eq(rascunhoId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/editais/rascunhos/{rascunhoId}", rascunhoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.nomeConcurso").value("Concurso Novo"));
    }

    @Test
    @DisplayName("Deve converter rascunho com status 201 Created")
    void deveConverterRascunho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rascunhoId = UUID.randomUUID();
        ConverterRascunhoEditalRequest request = new ConverterRascunhoEditalRequest("Plano BB", "Descricao");

        ConversaoEditalResponse response = new ConversaoEditalResponse(
                rascunhoId, UUID.randomUUID(), "Plano BB", 4, 12
        );

        when(editalParserService.converterEmPlanoEstudo(eq(userId), eq(rascunhoId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/editais/rascunhos/{rascunhoId}/converter", rascunhoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tituloPlano").value("Plano BB"))
                .andExpect(jsonPath("$.totalMateriasCriadas").value(4));
    }

    @Test
    @DisplayName("Deve excluir rascunho com status 204 No Content")
    void deveExcluirRascunho() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rascunhoId = UUID.randomUUID();

        doNothing().when(editalParserService).excluirRascunho(userId, rascunhoId);

        mockMvc.perform(delete("/api/v1/editais/rascunhos/{rascunhoId}", rascunhoId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString()))))
                .andExpect(status().isNoContent());
    }
}
