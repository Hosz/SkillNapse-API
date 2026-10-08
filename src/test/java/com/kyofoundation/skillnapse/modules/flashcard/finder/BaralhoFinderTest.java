package com.kyofoundation.skillnapse.modules.flashcard.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoMetricasProjection;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoRepository;
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
class BaralhoFinderTest {

    @Mock
    private BaralhoRepository baralhoRepository;

    @InjectMocks
    private BaralhoFinder baralhoFinder;

    @Test
    @DisplayName("Deve encontrar baralho por ID")
    void deveEncontrarPorId() {
        UUID id = UUID.randomUUID();
        Baralho baralho = Baralho.builder().id(id).build();

        when(baralhoRepository.findById(id)).thenReturn(Optional.of(baralho));

        Baralho resultado = baralhoFinder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando baralho nao for encontrado")
    void deveLancarExceptionQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(baralhoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> baralhoFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Baralho não encontrado.");
    }

    @Test
    @DisplayName("Deve buscar baralhos paginados por usuario")
    void deveBuscarPaginadoPorUsuario() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Baralho> pagina = new PageImpl<>(List.of(Baralho.builder().id(UUID.randomUUID()).build()));

        when(baralhoRepository.findByUsuarioId(userId, pageable)).thenReturn(pagina);

        Page<Baralho> resultado = baralhoFinder.buscarPorUsuario(userId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter metricas do baralho e por usuario")
    void deveObterMetricas() {
        UUID baralhoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();

        when(baralhoRepository.obterMetricasDoBaralho(baralhoId, hoje)).thenReturn(Optional.empty());
        when(baralhoRepository.obterMetricasPorUsuario(userId, hoje)).thenReturn(List.of());

        assertThat(baralhoFinder.obterMetricasDoBaralho(baralhoId, hoje)).isEmpty();
        assertThat(baralhoFinder.obterMetricasPorUsuario(userId, hoje)).isEmpty();

        verify(baralhoRepository).obterMetricasDoBaralho(baralhoId, hoje);
        verify(baralhoRepository).obterMetricasPorUsuario(userId, hoje);
    }
}
