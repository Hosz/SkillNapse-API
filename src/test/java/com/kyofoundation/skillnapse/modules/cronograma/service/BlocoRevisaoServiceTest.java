package com.kyofoundation.skillnapse.modules.cronograma.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AtualizarTopicosRevisaoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.finder.BlocoRevisaoFinder;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
import com.kyofoundation.skillnapse.modules.cronograma.validator.BlocoRevisaoValidator;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.repository.FlashcardRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlocoRevisaoServiceTest {

    @Mock
    private BlocoRevisaoFinder blocoRevisaoFinder;

    @Mock
    private BlocoRevisaoValidator blocoRevisaoValidator;

    @Mock
    private BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;

    @Mock
    private ExcecaoDiariaRepository excecaoDiariaRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private BlocoRevisaoService blocoRevisaoService;

    private Usuario usuario;
    private TemplateSemanal template;
    private Materia materia;
    private Topico topico;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
        template = TemplateSemanal.builder().id(UUID.randomUUID()).usuario(usuario).build();
        materia = Materia.builder().id(UUID.randomUUID()).nome("Direito Tributário").build();
        topico = Topico.builder().id(UUID.randomUUID()).titulo("Impostos Federais").materia(materia).build();
    }

    @Test
    @DisplayName("[criarBlocoRevisaoTemplate] Deve orquestrar a criação do bloco template de revisão")
    void deveCriarBlocoRevisaoTemplateComSucesso() {
        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                template.getId(),
                DiaSemana.QUINTA,
                LocalTime.of(19, 0),
                LocalTime.of(20, 30),
                materia.getId(),
                List.of(topico.getId())
        );

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(blocoRevisaoFinder.buscarTemplate(template.getId())).thenReturn(template);
        doNothing().when(blocoRevisaoValidator).validarCriacaoTemplate(request, template, usuario);
        when(blocoRevisaoFinder.buscarMateriaOpcional(materia.getId())).thenReturn(materia);
        doNothing().when(blocoRevisaoValidator).validarMateriaPertenceUsuario(materia, usuario);
        when(blocoRevisaoFinder.buscarTopicos(request.topicoIds())).thenReturn(List.of(topico));
        doNothing().when(blocoRevisaoValidator).validarTopicosRevisao(usuario, materia, List.of(topico), request.topicoIds());

        BlocoHorarioTemplate entitySalva = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.QUINTA)
                .horaInicio(LocalTime.of(19, 0))
                .horaFim(LocalTime.of(20, 30))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(new HashSet<>(Set.of(topico)))
                .build();

        when(blocoHorarioTemplateRepository.save(any(BlocoHorarioTemplate.class))).thenReturn(entitySalva);

        BlocoRevisaoResponse response = blocoRevisaoService.criarBlocoRevisaoTemplate(usuario.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(entitySalva.getId());
        assertThat(response.tipoBloco()).isEqualTo(TipoBloco.REVISAO);
        assertThat(response.topicosRevisao()).hasSize(1);
        verify(blocoHorarioTemplateRepository).save(any(BlocoHorarioTemplate.class));
    }

    @Test
    @DisplayName("[criarBlocoRevisaoDiario] Deve orquestrar a criação do bloco avulso de revisão")
    void deveCriarBlocoRevisaoDiarioComSucesso() {
        LocalDate data = LocalDate.of(2026, 10, 15);
        CriarBlocoRevisaoDiarioRequest request = new CriarBlocoRevisaoDiarioRequest(
                data,
                LocalTime.of(20, 0),
                LocalTime.of(21, 0),
                materia.getId(),
                List.of(topico.getId())
        );

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        doNothing().when(blocoRevisaoValidator).validarCriacaoDiaria(request);
        when(blocoRevisaoFinder.buscarMateriaOpcional(materia.getId())).thenReturn(materia);
        doNothing().when(blocoRevisaoValidator).validarMateriaPertenceUsuario(materia, usuario);
        when(blocoRevisaoFinder.buscarTopicos(request.topicoIds())).thenReturn(List.of(topico));
        doNothing().when(blocoRevisaoValidator).validarTopicosRevisao(usuario, materia, List.of(topico), request.topicoIds());

        ExcecaoDiaria entitySalva = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataExcecao(data)
                .horaInicio(LocalTime.of(20, 0))
                .horaFim(LocalTime.of(21, 0))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(new HashSet<>(Set.of(topico)))
                .build();

        when(excecaoDiariaRepository.save(any(ExcecaoDiaria.class))).thenReturn(entitySalva);

        BlocoRevisaoResponse response = blocoRevisaoService.criarBlocoRevisaoDiario(usuario.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(entitySalva.getId());
        assertThat(response.dataExcecao()).isEqualTo(data);
        assertThat(response.tipoBloco()).isEqualTo(TipoBloco.REVISAO);
        verify(excecaoDiariaRepository).save(any(ExcecaoDiaria.class));
    }

    @Test
    @DisplayName("[atualizarTopicosBlocoTemplate] Deve atualizar a lista de tópicos do bloco template")
    void deveAtualizarTopicosBlocoTemplate() {
        UUID blocoId = UUID.randomUUID();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(blocoId)
                .templateSemanal(template)
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(new HashSet<>())
                .build();

        AtualizarTopicosRevisaoRequest request = new AtualizarTopicosRevisaoRequest(List.of(topico.getId()));

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(blocoRevisaoFinder.buscarBlocoTemplate(blocoId)).thenReturn(bloco);
        doNothing().when(blocoRevisaoValidator).validarPropriedadeTemplate(bloco, usuario);
        doNothing().when(blocoRevisaoValidator).validarTipoBlocoRevisao(TipoBloco.REVISAO);
        when(blocoRevisaoFinder.buscarTopicos(request.topicoIds())).thenReturn(List.of(topico));
        doNothing().when(blocoRevisaoValidator).validarTopicosRevisao(usuario, materia, List.of(topico), request.topicoIds());
        when(blocoHorarioTemplateRepository.save(bloco)).thenReturn(bloco);

        BlocoRevisaoResponse response = blocoRevisaoService.atualizarTopicosBlocoTemplate(usuario.getId(), blocoId, request);

        assertThat(response).isNotNull();
        assertThat(bloco.getTopicosRevisao()).containsExactly(topico);
        verify(blocoHorarioTemplateRepository).save(bloco);
    }

    @Test
    @DisplayName("[atualizarTopicosBlocoDiario] Deve atualizar a lista de tópicos da exceção diária")
    void deveAtualizarTopicosBlocoDiario() {
        UUID excecaoId = UUID.randomUUID();
        ExcecaoDiaria excecao = ExcecaoDiaria.builder()
                .id(excecaoId)
                .usuario(usuario)
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(new HashSet<>())
                .build();

        AtualizarTopicosRevisaoRequest request = new AtualizarTopicosRevisaoRequest(List.of(topico.getId()));

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(blocoRevisaoFinder.buscarExcecaoDiaria(excecaoId)).thenReturn(excecao);
        doNothing().when(blocoRevisaoValidator).validarPropriedadeExcecao(excecao, usuario);
        doNothing().when(blocoRevisaoValidator).validarTipoBlocoRevisao(TipoBloco.REVISAO);
        when(blocoRevisaoFinder.buscarTopicos(request.topicoIds())).thenReturn(List.of(topico));
        doNothing().when(blocoRevisaoValidator).validarTopicosRevisao(usuario, materia, List.of(topico), request.topicoIds());
        when(excecaoDiariaRepository.save(excecao)).thenReturn(excecao);

        BlocoRevisaoResponse response = blocoRevisaoService.atualizarTopicosBlocoDiario(usuario.getId(), excecaoId, request);

        assertThat(response).isNotNull();
        assertThat(excecao.getTopicosRevisao()).containsExactly(topico);
        verify(excecaoDiariaRepository).save(excecao);
    }

    @Test
    @DisplayName("[obterConteudoRevisaoTemplate] Deve carregar flashcards restritos aos tópicos autorizados do bloco")
    void deveObterConteudoRevisaoTemplateRestritoAosTopicos() {
        UUID blocoId = UUID.randomUUID();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(blocoId)
                .templateSemanal(template)
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .horaInicio(LocalTime.of(14, 0))
                .horaFim(LocalTime.of(15, 30))
                .topicosRevisao(new HashSet<>(Set.of(topico)))
                .build();

        LocalDate dataRef = LocalDate.of(2026, 10, 8);
        Baralho baralho = Baralho.builder().id(UUID.randomUUID()).titulo("Tributário").build();
        Flashcard card = Flashcard.builder()
                .id(UUID.randomUUID())
                .baralho(baralho)
                .topico(topico)
                .frente("O que é IPI?")
                .verso("Imposto sobre Produtos Industrializados")
                .proximaRevisao(dataRef)
                .build();

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(blocoRevisaoFinder.buscarBlocoTemplate(blocoId)).thenReturn(bloco);
        doNothing().when(blocoRevisaoValidator).validarPropriedadeTemplate(bloco, usuario);
        doNothing().when(blocoRevisaoValidator).validarTipoBlocoRevisao(TipoBloco.REVISAO);

        when(flashcardRepository.buscarCardsVencidosPorTopicos(eq(usuario.getId()), eq(List.of(topico.getId())), eq(dataRef)))
                .thenReturn(List.of(card));
        when(flashcardRepository.contarCardsPorTopicos(eq(usuario.getId()), eq(List.of(topico.getId()))))
                .thenReturn(5L);

        ConteudoBlocoRevisaoResponse conteudo = blocoRevisaoService.obterConteudoRevisaoTemplate(usuario.getId(), blocoId, dataRef);

        assertThat(conteudo).isNotNull();
        assertThat(conteudo.blocoId()).isEqualTo(blocoId);
        assertThat(conteudo.totalTopicos()).isEqualTo(1);
        assertThat(conteudo.totalCardsCadastrados()).isEqualTo(5L);
        assertThat(conteudo.totalCardsParaRevisar()).isEqualTo(1);
        assertThat(conteudo.cardsParaRevisar().get(0).frente()).isEqualTo("O que é IPI?");
        assertThat(conteudo.cardsParaRevisar().get(0).topicoId()).isEqualTo(topico.getId());
    }

    @Test
    @DisplayName("[obterConteudoRevisaoDiario] Deve carregar flashcards restritos da exceção diária")
    void deveObterConteudoRevisaoDiarioRestritoAosTopicos() {
        UUID excecaoId = UUID.randomUUID();
        LocalDate dataExcecao = LocalDate.of(2026, 10, 10);
        ExcecaoDiaria excecao = ExcecaoDiaria.builder()
                .id(excecaoId)
                .usuario(usuario)
                .dataExcecao(dataExcecao)
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .horaInicio(LocalTime.of(16, 0))
                .horaFim(LocalTime.of(17, 0))
                .topicosRevisao(new HashSet<>(Set.of(topico)))
                .build();

        when(blocoRevisaoFinder.buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(blocoRevisaoFinder.buscarExcecaoDiaria(excecaoId)).thenReturn(excecao);
        doNothing().when(blocoRevisaoValidator).validarPropriedadeExcecao(excecao, usuario);
        doNothing().when(blocoRevisaoValidator).validarTipoBlocoRevisao(TipoBloco.REVISAO);

        when(flashcardRepository.buscarCardsVencidosPorTopicos(eq(usuario.getId()), eq(List.of(topico.getId())), eq(dataExcecao)))
                .thenReturn(List.of());
        when(flashcardRepository.contarCardsPorTopicos(eq(usuario.getId()), eq(List.of(topico.getId()))))
                .thenReturn(3L);

        ConteudoBlocoRevisaoResponse conteudo = blocoRevisaoService.obterConteudoRevisaoDiario(usuario.getId(), excecaoId, null);

        assertThat(conteudo).isNotNull();
        assertThat(conteudo.blocoId()).isEqualTo(excecaoId);
        assertThat(conteudo.dataReferencia()).isEqualTo(dataExcecao);
        assertThat(conteudo.totalCardsCadastrados()).isEqualTo(3L);
        assertThat(conteudo.totalCardsParaRevisar()).isEqualTo(0);
        assertThat(conteudo.cardsParaRevisar()).isEmpty();
    }
}
