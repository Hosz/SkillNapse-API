package com.kyofoundation.skillnapse.modules.flashcard.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.RevisarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.FlashcardResponse;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.HistoricoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.entity.HistoricoRevisaoFlashcard;
import com.kyofoundation.skillnapse.modules.flashcard.enums.ClassificacaoResposta;
import com.kyofoundation.skillnapse.modules.flashcard.finder.BaralhoFinder;
import com.kyofoundation.skillnapse.modules.flashcard.finder.FlashcardFinder;
import com.kyofoundation.skillnapse.modules.flashcard.finder.HistoricoRevisaoFinder;
import com.kyofoundation.skillnapse.modules.flashcard.repository.FlashcardRepository;
import com.kyofoundation.skillnapse.modules.flashcard.repository.HistoricoRevisaoFlashcardRepository;
import com.kyofoundation.skillnapse.modules.flashcard.support.SrsAlgorithmSupport;
import com.kyofoundation.skillnapse.modules.flashcard.validator.BaralhoValidator;
import com.kyofoundation.skillnapse.modules.flashcard.validator.FlashcardValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private BaralhoFinder baralhoFinder;

    @Mock
    private TopicoFinder topicoFinder;

    @Mock
    private FlashcardFinder flashcardFinder;

    @Mock
    private HistoricoRevisaoFinder historicoRevisaoFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private BaralhoValidator baralhoValidator;

    @Mock
    private FlashcardValidator flashcardValidator;

    @Mock
    private SrsAlgorithmSupport srsAlgorithmSupport;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private HistoricoRevisaoFlashcardRepository historicoRevisaoRepository;

    @InjectMocks
    private FlashcardService flashcardService;

    @Test
    @DisplayName("Deve criar flashcard com sucesso")
    void deveCriarFlashcardComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        CriarFlashcardRequest request = new CriarFlashcardRequest(baralhoId, topicoId, "Frente", "Verso");

        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().id(baralhoId).usuario(usuario).build();
        Topico topico = Topico.builder().id(topicoId).build();
        Flashcard salvo = Flashcard.builder().id(UUID.randomUUID()).baralho(baralho).topico(topico).frente("Frente").verso("Verso").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(baralhoFinder.findById(baralhoId)).thenReturn(baralho);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        when(flashcardRepository.save(any(Flashcard.class))).thenReturn(salvo);

        FlashcardResponse response = flashcardService.criarFlashcard(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.frente()).isEqualTo("Frente");
        verify(flashcardValidator).validarCriacao(request);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(baralhoValidator).validarPropriedadeBaralho(usuario, baralho);
        verify(flashcardValidator).validarTopicoPertenceUsuario(usuario, topico);
        verify(flashcardRepository).save(any(Flashcard.class));
    }

    @Test
    @DisplayName("Deve atualizar flashcard existente")
    void deveAtualizarFlashcardExistente() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        AtualizarFlashcardRequest request = new AtualizarFlashcardRequest(null, "Nova Frente", "Novo Verso");

        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();
        Flashcard flashcard = Flashcard.builder().id(cardId).baralho(baralho).frente("Antiga").verso("Antigo").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(flashcardFinder.findById(cardId)).thenReturn(flashcard);
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        FlashcardResponse response = flashcardService.atualizarFlashcard(userId, cardId, request);

        assertThat(response).isNotNull();
        assertThat(flashcard.getFrente()).isEqualTo("Nova Frente");
        verify(flashcardValidator).validarAtualizacao(request);
        verify(flashcardValidator).validarPropriedadeFlashcard(usuario, flashcard);
    }

    @Test
    @DisplayName("Deve revisar flashcard aplicando algoritmo SM-2 e salvando historico")
    void deveRevisarFlashcardComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        RevisarFlashcardRequest request = new RevisarFlashcardRequest(ClassificacaoResposta.BOM, 7);

        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();
        Flashcard flashcard = Flashcard.builder()
                .id(cardId)
                .baralho(baralho)
                .fatorFacilidade(new BigDecimal("2.50"))
                .intervaloDias(1)
                .repeticoes(1)
                .build();

        LocalDate proxima = LocalDate.now().plusDays(6);
        SrsAlgorithmSupport.SrsResultadoCalculo resultadoSrs = new SrsAlgorithmSupport.SrsResultadoCalculo(
                new BigDecimal("2.50"), 6, 2, proxima
        );

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(flashcardFinder.findById(cardId)).thenReturn(flashcard);
        when(srsAlgorithmSupport.calcularProximaRevisao(eq(flashcard), eq(ClassificacaoResposta.BOM), any(LocalDate.class)))
                .thenReturn(resultadoSrs);
        when(flashcardRepository.save(flashcard)).thenReturn(flashcard);

        FlashcardResponse response = flashcardService.revisarFlashcard(userId, cardId, request);

        assertThat(response).isNotNull();
        assertThat(flashcard.getIntervaloDias()).isEqualTo(6);
        assertThat(flashcard.getRepeticoes()).isEqualTo(2);
        assertThat(flashcard.getProximaRevisao()).isEqualTo(proxima);

        verify(flashcardValidator).validarRevisao(request);
        verify(flashcardValidator).validarPropriedadeFlashcard(usuario, flashcard);
        verify(flashcardRepository).save(flashcard);
        verify(historicoRevisaoRepository).save(any(HistoricoRevisaoFlashcard.class));
    }

    @Test
    @DisplayName("Deve listar cards vencidos para revisao (/due)")
    void deveListarCardsVencidos() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuario = Usuario.builder().id(userId).build();
        Flashcard card = Flashcard.builder().id(UUID.randomUUID()).baralho(Baralho.builder().build()).build();
        Page<Flashcard> pagina = new PageImpl<>(List.of(card), pageable, 1);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(flashcardFinder.buscarCardsVencidos(eq(userId), any(LocalDate.class), eq(null), eq(null), eq(pageable)))
                .thenReturn(pagina);

        Page<FlashcardResponse> resultado = flashcardService.listarCardsVencidos(userId, null, null, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve apagar flashcard com sucesso")
    void deveApagarFlashcard() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();
        Flashcard flashcard = Flashcard.builder().id(cardId).baralho(baralho).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(flashcardFinder.findById(cardId)).thenReturn(flashcard);

        flashcardService.apagarFlashcard(userId, cardId);

        verify(flashcardValidator).validarPropriedadeFlashcard(usuario, flashcard);
        verify(flashcardRepository).delete(flashcard);
    }

    @Test
    @DisplayName("Deve listar historico de revisoes do flashcard")
    void deveListarHistoricoRevisoes() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().usuario(usuario).build();
        Flashcard flashcard = Flashcard.builder().id(cardId).baralho(baralho).build();

        HistoricoRevisaoFlashcard hist = HistoricoRevisaoFlashcard.builder()
                .id(UUID.randomUUID())
                .flashcard(flashcard)
                .classificacaoResposta(ClassificacaoResposta.FACIL)
                .build();
        Page<HistoricoRevisaoFlashcard> pagina = new PageImpl<>(List.of(hist), pageable, 1);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(flashcardFinder.findById(cardId)).thenReturn(flashcard);
        when(historicoRevisaoFinder.buscarPorFlashcard(cardId, pageable)).thenReturn(pagina);

        Page<HistoricoRevisaoResponse> resultado = flashcardService.listarHistoricoRevisoes(userId, cardId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }
}
