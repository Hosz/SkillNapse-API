package com.kyofoundation.skillnapse.e2e;

import com.kyofoundation.skillnapse.TestcontainersConfiguration;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.TemplateSemanalService;
import com.kyofoundation.skillnapse.modules.desempenho.service.DesempenhoService;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.service.BaralhoService;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.PlanoEstudoRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.service.MateriaService;
import com.kyofoundation.skillnapse.modules.planoestudo.service.PlanoEstudoService;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import com.kyofoundation.skillnapse.modules.redacao.repository.SubmissaoRedacaoRepository;
import com.kyofoundation.skillnapse.modules.redacao.repository.TemaRedacaoRepository;
import com.kyofoundation.skillnapse.modules.redacao.service.SubmissaoRedacaoService;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.enums.StatusSessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.repository.SessaoEstudoRepository;
import com.kyofoundation.skillnapse.modules.sessao.service.SessaoEstudoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IsolamentoMultiusuarioIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlanoEstudoService planoEstudoService;

    @Autowired
    private PlanoEstudoRepository planoEstudoRepository;

    @Autowired
    private MateriaService materiaService;

    @Autowired
    private com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository materiaRepository;

    @Autowired
    private com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository topicoRepository;

    @Autowired
    private TemplateSemanalService templateSemanalService;

    @Autowired
    private SessaoEstudoService sessaoEstudoService;

    @Autowired
    private SessaoEstudoRepository sessaoEstudoRepository;

    @Autowired
    private BaralhoService baralhoService;

    @Autowired
    private SubmissaoRedacaoService submissaoRedacaoService;

    @Autowired
    private SubmissaoRedacaoRepository submissaoRedacaoRepository;

    @Autowired
    private TemaRedacaoRepository temaRedacaoRepository;

    @Autowired
    private DesempenhoService desempenhoService;

    private Usuario usuarioA;
    private Usuario usuarioB;

    @BeforeEach
    void setUp() {
        usuarioA = usuarioRepository.save(Usuario.builder()
                .nome("Usuario A")
                .email("user.a." + UUID.randomUUID() + "@skillnapse.com")
                .senhaHash(passwordEncoder.encode("senha123"))
                .ativo(true)
                .build());

        usuarioB = usuarioRepository.save(Usuario.builder()
                .nome("Usuario B")
                .email("user.b." + UUID.randomUUID() + "@skillnapse.com")
                .senhaHash(passwordEncoder.encode("senha123"))
                .ativo(true)
                .build());
    }

    @Test
    @DisplayName("Usuário B não deve conseguir acessar, modificar ou deletar plano de estudo do Usuário A")
    void deveBloquearAcessoAoPlanoDeOutroUsuario() {
        PlanoEstudoResponse planoA = planoEstudoService.criarPlano(
                new PlanoEstudoRequest("Plano Concurso TCU", "Foco Auditor"),
                usuarioA.getId()
        );

        // Usuário B tenta obter plano de A
        assertThatThrownBy(() -> planoEstudoService.visualizarPlano(usuarioB.getId(), planoA.id()))
                .isInstanceOf(ForbiddenException.class);

        // Usuário B tenta editar plano de A
        assertThatThrownBy(() -> planoEstudoService.editarPlano(
                usuarioB.getId(),
                planoA.id(),
                new EdicaoPlanoEstudoRequest("Hacked", "Descricao", false)
        )).isInstanceOf(ForbiddenException.class);

        // Usuário B tenta criar matéria dentro do plano de A
        assertThatThrownBy(() -> materiaService.criarMateria(
                usuarioB.getId(),
                planoA.id(),
                new CriarMateriaRequest("Direito Constitucional", "#FF0000", 1)
        )).isInstanceOf(ForbiddenException.class);

        // Usuário B tenta obter relatório de lacunas do plano de A
        assertThatThrownBy(() -> desempenhoService.obterRelatorioLacunas(usuarioB.getId(), planoA.id()))
                .isInstanceOf(ForbiddenException.class);

        // Usuário B tenta deletar plano de A
        assertThatThrownBy(() -> planoEstudoService.apagarPlano(usuarioB.getId(), planoA.id()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Usuário B não deve conseguir acessar template de cronograma semanal do Usuário A")
    void deveBloquearAcessoAoCronogramaDeOutroUsuario() {
        TemplateSemanalResponse templateA = templateSemanalService.criarTemplate(
                usuarioA.getId(),
                new CriarTemplateSemanalRequest("Grade Principal", true)
        );

        assertThatThrownBy(() -> templateSemanalService.visualizarTemplate(usuarioB.getId(), templateA.id()))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> templateSemanalService.ativarTemplate(usuarioB.getId(), templateA.id()))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> templateSemanalService.apagarTemplate(usuarioB.getId(), templateA.id()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Usuário B não deve conseguir visualizar sessão de estudo do Usuário A")
    void deveBloquearAcessoASessaoDeOutroUsuario() {
        PlanoEstudo planoA = planoEstudoRepository.save(PlanoEstudo.builder()
                .usuario(usuarioA)
                .titulo("Plano de A para Sessão")
                .descricao("Desc")
                .build());

        com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia materiaA = materiaRepository.save(com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia.builder()
                .planoEstudo(planoA)
                .nome("Materia A")
                .corHex("#123456")
                .ordem(1)
                .build());

        com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico topicoA = topicoRepository.save(com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico.builder()
                .materia(materiaA)
                .titulo("Topico A")
                .ordem(1)
                .pesoEdital(5)
                .nivelProficiencia(com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia.INICIANTE)
                .concluido(false)
                .build());

        SessaoEstudo sessaoA = sessaoEstudoRepository.save(SessaoEstudo.builder()
                .usuario(usuarioA)
                .topico(topicoA)
                .iniciadoEm(Instant.now().minusSeconds(1800))
                .finalizadoEm(Instant.now())
                .duracaoLiquidaSegundos(1800)
                .status(StatusSessaoEstudo.CONCLUIDA)
                .observacoes("Sessao Usuario A")
                .build());

        assertThatThrownBy(() -> sessaoEstudoService.visualizarSessaoEstudo(usuarioB.getId(), sessaoA.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Usuário B não deve conseguir acessar baralho de flashcards do Usuário A")
    void deveBloquearAcessoAoBaralhoDeOutroUsuario() {
        BaralhoResponse baralhoA = baralhoService.criarBaralho(
                usuarioA.getId(),
                new CriarBaralhoRequest("Baralho Direito", "Flashcards da matéria", null)
        );

        assertThatThrownBy(() -> baralhoService.obterBaralho(usuarioB.getId(), baralhoA.id()))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> baralhoService.apagarBaralho(usuarioB.getId(), baralhoA.id()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Usuário B não deve conseguir submeter redação para tema de A nem ler sua submissão")
    void deveBloquearAcessoARedacaoDeOutroUsuario() {
        PlanoEstudo planoEntity = planoEstudoRepository.save(PlanoEstudo.builder()
                .usuario(usuarioA)
                .titulo("Plano Privado")
                .descricao("Privado")
                .build());

        TemaRedacao temaA = temaRedacaoRepository.save(TemaRedacao.builder()
                .planoEstudo(planoEntity)
                .titulo("Tema Privado de A")
                .textosMotivadores("Motivador...")
                .criteriosAvaliacao("Critérios...")
                .build());

        // Usuário B tenta submeter redação para tema do plano privado de A
        assertThatThrownBy(() -> submissaoRedacaoService.submeterRedacao(
                usuarioB.getId(),
                new SubmeterRedacaoRequest(temaA.getId(), "Texto dissertativo com mais de 300 caracteres...".repeat(10))
        )).isInstanceOf(ForbiddenException.class);

        // Submissão pertencente a A
        SubmissaoRedacao submissaoA = submissaoRedacaoRepository.save(SubmissaoRedacao.builder()
                .usuario(usuarioA)
                .temaRedacao(temaA)
                .textoAluno("Texto de redação do usuario A com mais de 300 caracteres...".repeat(8))
                .notaGeral(new BigDecimal("950.00"))
                .feedbackIaJson("{\"notaGeral\":950.0}")
                .criadoEm(Instant.now())
                .corrigidoEm(Instant.now())
                .build());

        // Usuário B tenta ler submissão de A
        assertThatThrownBy(() -> submissaoRedacaoService.obterPorId(usuarioB.getId(), submissaoA.getId()))
                .isInstanceOf(ForbiddenException.class);
    }
}
