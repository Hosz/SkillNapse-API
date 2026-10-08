package com.kyofoundation.skillnapse.modules.flashcard.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.repository.FlashcardRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardFinderTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @InjectMocks
    private FlashcardFinder flashcardFinder;

    @Test
    @DisplayName("Deve encontrar flashcard por ID")
    void deveEncontrarPorId() {
        UUID id = UUID.randomUUID();
        Flashcard card = Flashcard.builder().id(id).build();

        when(flashcardRepository.findById(id)).thenReturn(Optional.of(card));

        Flashcard resultado = flashcardFinder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando flashcard nao for encontrado")
    void deveLancarExceptionQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(flashcardRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flashcardFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Flashcard não encontrado.");
    }

    @Test
    @DisplayName("Deve buscar flashcards paginados por baralho e topico")
    void deveBuscarPaginadoPorBaralhoETopico() {
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flashcard> pagina = new PageImpl<>(List.of(Flashcard.builder().id(id).build()));

        when(flashcardRepository.findByBaralhoId(id, pageable)).thenReturn(pagina);
        when(flashcardRepository.findByTopicoId(id, pageable)).thenReturn(pagina);

        assertThat(flashcardFinder.buscarPorBaralho(id, pageable)).isEqualTo(pagina);
        assertThat(flashcardFinder.buscarPorTopico(id, pageable)).isEqualTo(pagina);
    }

    @Test
    @DisplayName("Deve buscar cards vencidos e contar vencidos")
    void deveBuscarCardsVencidosEContar() {
        UUID userId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flashcard> pagina = new PageImpl<>(List.of());

        when(flashcardRepository.buscarCardsVencidos(userId, hoje, null, null, pageable)).thenReturn(pagina);
        when(flashcardRepository.contarCardsVencidos(userId, hoje)).thenReturn(5L);

        Page<Flashcard> resultado = flashcardFinder.buscarCardsVencidos(userId, hoje, null, null, pageable);
        long total = flashcardFinder.contarCardsVencidos(userId, hoje);

        assertThat(resultado).isEqualTo(pagina);
        assertThat(total).isEqualTo(5L);
        verify(flashcardRepository).buscarCardsVencidos(userId, hoje, null, null, pageable);
        verify(flashcardRepository).contarCardsVencidos(userId, hoje);
    }
}
