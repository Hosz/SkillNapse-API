package com.kyofoundation.skillnapse.modules.gamificacao.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.MetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.PainelGamificacaoResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.ProgressoMetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import com.kyofoundation.skillnapse.modules.gamificacao.finder.MetaDiariaFinder;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.MetaDiariaRepository;
import com.kyofoundation.skillnapse.modules.gamificacao.support.ProgressoMetaDiariaSupport;
import com.kyofoundation.skillnapse.modules.gamificacao.validator.MetaDiariaValidator;
import com.kyofoundation.skillnapse.modules.questao.repository.TentativaQuestaoRepository;
import com.kyofoundation.skillnapse.modules.sessao.repository.SessaoEstudoRepository;
import com.kyofoundation.skillnapse.modules.sessao.repository.TotalizadorSessaoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaDiariaServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private MetaDiariaFinder metaDiariaFinder;

    @Mock
    private MetaDiariaValidator metaDiariaValidator;

    @Mock
    private MetaDiariaRepository metaDiariaRepository;

    @Mock
    private SessaoEstudoRepository sessaoEstudoRepository;

    @Mock
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Mock
    private ProgressoMetaDiariaSupport progressoMetaDiariaSupport;

    @Mock
    private OfensivaService ofensivaService;

    @Mock
    private com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator usuarioValidator;

    @InjectMocks
    private MetaDiariaService metaDiariaService;

    private Usuario usuario;
    private MetaDiaria meta;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
        meta = MetaDiaria.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .metaMinutosEstudo(120)
                .metaQuestoesResolvidas(15)
                .build();
    }

    @Test
    @DisplayName("[obterConfiguracaoMetas] Deve retornar configuração das metas do usuário")
    void deveObterConfiguracaoMetas() {
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(metaDiariaFinder.buscarOuCriar(usuario)).thenReturn(meta);

        MetaDiariaResponse response = metaDiariaService.obterConfiguracaoMetas(usuario.getId());

        assertThat(response).isNotNull();
        assertThat(response.metaMinutosEstudo()).isEqualTo(120);
        assertThat(response.metaQuestoesResolvidas()).isEqualTo(15);
    }

    @Test
    @DisplayName("[atualizarMetas] Deve validar, atualizar e persistir metas")
    void deveAtualizarMetasComSucesso() {
        AtualizarMetaDiariaRequest request = new AtualizarMetaDiariaRequest(180, 25);

        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        doNothing().when(metaDiariaValidator).validarAtualizacao(request);
        when(metaDiariaFinder.buscarOuCriar(usuario)).thenReturn(meta);
        when(metaDiariaRepository.save(meta)).thenReturn(meta);

        MetaDiariaResponse response = metaDiariaService.atualizarMetas(usuario.getId(), request);

        assertThat(response).isNotNull();
        assertThat(meta.getMetaMinutosEstudo()).isEqualTo(180);
        assertThat(meta.getMetaQuestoesResolvidas()).isEqualTo(25);
        verify(metaDiariaRepository).save(meta);
    }

    @Test
    @DisplayName("[obterProgressoDiario] Deve agregar sessões e questões e calcular progresso diário")
    void deveObterProgressoDiario() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        TotalizadorSessaoProjection projection = mock(TotalizadorSessaoProjection.class);
        when(projection.getTotalSegundos()).thenReturn(7200L); // 120 min

        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(metaDiariaFinder.buscarOuCriar(usuario)).thenReturn(meta);
        when(sessaoEstudoRepository.obterDadosAgregados(eq(usuario.getId()), isNull(), any(Instant.class), any(Instant.class)))
                .thenReturn(projection);
        when(tentativaQuestaoRepository.contarQuestoesRespondidasNoIntervalo(eq(usuario.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(15L);

        ProgressoMetaDiariaResponse progressoCalculado = new ProgressoMetaDiariaResponse(
                hoje, 120, 120L, 100.0, true, 15, 15L, 100.0, true, true
        );
        when(progressoMetaDiariaSupport.calcularProgresso(meta, 7200L, 15L, hoje))
                .thenReturn(progressoCalculado);

        ProgressoMetaDiariaResponse response = metaDiariaService.obterProgressoDiario(usuario.getId(), hoje);

        assertThat(response).isNotNull();
        assertThat(response.todasMetasAtingidas()).isTrue();
    }

    @Test
    @DisplayName("[obterPainelCompleto] Deve compor painel com ofensiva e metas")
    void deveObterPainelCompleto() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        StatusOfensivaResponse ofensivaResp = new StatusOfensivaResponse(
                UUID.randomUUID(), usuario.getId(), 2, 5, hoje, true, true
        );
        when(ofensivaService.obterStatusOfensiva(usuario.getId())).thenReturn(ofensivaResp);

        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(metaDiariaFinder.buscarOuCriar(usuario)).thenReturn(meta);
        when(sessaoEstudoRepository.obterDadosAgregados(eq(usuario.getId()), isNull(), any(Instant.class), any(Instant.class)))
                .thenReturn(null);
        when(tentativaQuestaoRepository.contarQuestoesRespondidasNoIntervalo(eq(usuario.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(0L);

        ProgressoMetaDiariaResponse progressoCalculado = new ProgressoMetaDiariaResponse(
                hoje, 120, 0L, 0.0, false, 15, 0L, 0.0, false, false
        );
        when(progressoMetaDiariaSupport.calcularProgresso(meta, 0L, 0L, hoje))
                .thenReturn(progressoCalculado);

        PainelGamificacaoResponse painel = metaDiariaService.obterPainelCompleto(usuario.getId(), hoje);

        assertThat(painel).isNotNull();
        assertThat(painel.ofensiva().diasConsecutivosAtual()).isEqualTo(2);
        assertThat(painel.progressoHoje().minutosEstudadosHoje()).isEqualTo(0L);
    }

    @Test
    @DisplayName("[obterConfiguracaoMetas] Deve lançar ForbiddenException quando usuário inativo")
    void deveLancarForbiddenAoConsultarMetasComUsuarioInativo() {
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        doThrow(new com.kyofoundation.skillnapse.common.exception.ForbiddenException("Usuário inativo ou bloqueado no sistema."))
                .when(usuarioValidator).validarUsuarioAtivo(usuario);

        assertThatThrownBy(() -> metaDiariaService.obterConfiguracaoMetas(usuario.getId()))
                .isInstanceOf(com.kyofoundation.skillnapse.common.exception.ForbiddenException.class)
                .hasMessage("Usuário inativo ou bloqueado no sistema.");
    }
}
