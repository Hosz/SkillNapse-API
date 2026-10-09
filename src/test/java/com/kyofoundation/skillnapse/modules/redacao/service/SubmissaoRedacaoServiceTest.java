package com.kyofoundation.skillnapse.modules.redacao.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.AvaliacaoCompetenciaIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.SugestaoReescritaIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResponse;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResumoResponse;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import com.kyofoundation.skillnapse.modules.redacao.finder.SubmissaoRedacaoFinder;
import com.kyofoundation.skillnapse.modules.redacao.finder.TemaRedacaoFinder;
import com.kyofoundation.skillnapse.modules.redacao.repository.SubmissaoRedacaoRepository;
import com.kyofoundation.skillnapse.modules.redacao.support.JsonFeedbackRedacaoSupport;
import com.kyofoundation.skillnapse.modules.redacao.support.PromptCorrecaoRedacaoSupport;
import com.kyofoundation.skillnapse.modules.redacao.validator.SubmissaoRedacaoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissaoRedacaoServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private TemaRedacaoFinder temaRedacaoFinder;

    @Mock
    private SubmissaoRedacaoValidator submissaoRedacaoValidator;

    @Mock
    private SubmissaoRedacaoFinder submissaoRedacaoFinder;

    @Mock
    private SubmissaoRedacaoRepository submissaoRedacaoRepository;

    @Mock
    private PromptCorrecaoRedacaoSupport promptCorrecaoRedacaoSupport;

    @Mock
    private AiOrchestratorService aiOrchestratorService;

    @Mock
    private JsonFeedbackRedacaoSupport jsonFeedbackRedacaoSupport;

    @InjectMocks
    private SubmissaoRedacaoService submissaoRedacaoService;

    private Usuario usuario;
    private TemaRedacao tema;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Estudante Redação")
                .ativo(true)
                .build();

        tema = TemaRedacao.builder()
                .id(UUID.randomUUID())
                .titulo("Os Desafios da IA no Setor Público")
                .textosMotivadores("Texto I e II sobre automação e ética.")
                .criteriosAvaliacao("Padrão dissertativo-argumentativo formal.")
                .build();
    }

    @Test
    @DisplayName("Deve submeter redação e processar correção analítica com sucesso")
    void deveSubmeterRedacaoComSucesso() {
        String texto = "A modernização da administração pública brasileira requer a incorporação prudente de inteligência artificial...".repeat(4);
        SubmeterRedacaoRequest request = new SubmeterRedacaoRequest(tema.getId(), texto);

        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(temaRedacaoFinder.findById(tema.getId())).thenReturn(tema);
        when(promptCorrecaoRedacaoSupport.construirPromptCorrecao(tema, texto)).thenReturn("Prompt Correcao");

        FeedbackCorrecaoIaPayload feedback = new FeedbackCorrecaoIaPayload(
                8.5,
                List.of(new AvaliacaoCompetenciaIaPayload("Gramática", 8.5, "Bom vocabulário", List.of())),
                "Texto claro e bem articulado.",
                List.of(new SugestaoReescritaIaPayload("Trecho A", "Trecho B", "Justificativa"))
        );
        when(aiOrchestratorService.generateStructured(any(PromptRequest.class), eq(FeedbackCorrecaoIaPayload.class)))
                .thenReturn(feedback);
        when(jsonFeedbackRedacaoSupport.serializar(feedback)).thenReturn("{\"notaGeral\":8.5}");

        UUID submissaoId = UUID.randomUUID();
        when(submissaoRedacaoRepository.save(any(SubmissaoRedacao.class))).thenAnswer(invocation -> {
            SubmissaoRedacao s = invocation.getArgument(0);
            s.setId(submissaoId);
            s.setCriadoEm(Instant.now());
            return s;
        });

        SubmissaoRedacaoResponse response = submissaoRedacaoService.submeterRedacao(usuario.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(submissaoId);
        assertThat(response.temaId()).isEqualTo(tema.getId());
        assertThat(response.notaGeral()).isEqualTo(new BigDecimal("8.50"));
        assertThat(response.feedback()).isEqualTo(feedback);

        verify(submissaoRedacaoValidator).validarRequisicao(request);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(submissaoRedacaoValidator).validarPropriedadeTema(usuario, tema);
        verify(submissaoRedacaoValidator).validarFeedbackIa(feedback);
        verify(submissaoRedacaoRepository).save(any(SubmissaoRedacao.class));
    }

    @Test
    @DisplayName("Deve listar submissões do usuário paginadas com filtro por tema")
    void deveListarSubmissoesPorTema() {
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(temaRedacaoFinder.findById(tema.getId())).thenReturn(tema);

        SubmissaoRedacao s1 = SubmissaoRedacao.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .temaRedacao(tema)
                .notaGeral(new BigDecimal("9.00"))
                .criadoEm(Instant.now())
                .corrigidoEm(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(submissaoRedacaoRepository.findByUsuarioAndTemaRedacaoOrderByCriadoEmDesc(usuario, tema, pageable))
                .thenReturn(new PageImpl<>(List.of(s1), pageable, 1));

        Page<SubmissaoRedacaoResumoResponse> resultado = submissaoRedacaoService.listar(usuario.getId(), tema.getId(), pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().notaGeral()).isEqualTo(new BigDecimal("9.00"));

        verify(submissaoRedacaoValidator).validarPropriedadeTema(usuario, tema);
    }

    @Test
    @DisplayName("Deve obter submissão por ID com feedback desserializado")
    void deveObterSubmissaoPorId() {
        UUID submissaoId = UUID.randomUUID();
        SubmissaoRedacao submissao = SubmissaoRedacao.builder()
                .id(submissaoId)
                .usuario(usuario)
                .temaRedacao(tema)
                .textoAluno("Texto da redação com desenvolvimento detalhado.")
                .notaGeral(new BigDecimal("9.50"))
                .feedbackIaJson("{\"notaGeral\":9.5}")
                .criadoEm(Instant.now())
                .corrigidoEm(Instant.now())
                .build();

        FeedbackCorrecaoIaPayload feedback = new FeedbackCorrecaoIaPayload(
                9.5, List.of(), "Excelente", List.of()
        );

        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(submissaoRedacaoFinder.findById(submissaoId)).thenReturn(submissao);
        when(jsonFeedbackRedacaoSupport.desserializar("{\"notaGeral\":9.5}")).thenReturn(feedback);

        SubmissaoRedacaoResponse response = submissaoRedacaoService.obterPorId(usuario.getId(), submissaoId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(submissaoId);
        assertThat(response.notaGeral()).isEqualTo(new BigDecimal("9.50"));
        assertThat(response.feedback()).isEqualTo(feedback);

        verify(submissaoRedacaoValidator).validarPropriedadeSubmissao(usuario, submissao);
    }
}
