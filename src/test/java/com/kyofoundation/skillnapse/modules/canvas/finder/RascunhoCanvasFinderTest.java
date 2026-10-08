package com.kyofoundation.skillnapse.modules.canvas.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.canvas.repository.RascunhoCanvasRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RascunhoCanvasFinderTest {

    @Mock
    private RascunhoCanvasRepository rascunhoCanvasRepository;

    @InjectMocks
    private RascunhoCanvasFinder rascunhoCanvasFinder;

    @Test
    @DisplayName("Deve encontrar rascunho de canvas por ID com sucesso")
    void deveEncontrarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        RascunhoCanvas entity = RascunhoCanvas.builder().id(id).build();

        when(rascunhoCanvasRepository.findById(id)).thenReturn(Optional.of(entity));

        RascunhoCanvas resultado = rascunhoCanvasFinder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        verify(rascunhoCanvasRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando rascunho nao for encontrado por ID")
    void deveLancarExceptionQuandoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(rascunhoCanvasRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rascunhoCanvasFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rascunho de canvas não encontrado.");
    }

    @Test
    @DisplayName("Deve encontrar rascunho de canvas por Usuario e Topico com sucesso")
    void deveEncontrarPorUsuarioETopicoComSucesso() {
        UUID usuarioId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        RascunhoCanvas entity = RascunhoCanvas.builder().id(UUID.randomUUID()).build();

        when(rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId)).thenReturn(Optional.of(entity));

        RascunhoCanvas resultado = rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(usuarioId, topicoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(entity.getId());
        verify(rascunhoCanvasRepository).findByUsuarioIdAndTopicoId(usuarioId, topicoId);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando rascunho nao for encontrado por Usuario e Topico")
    void deveLancarExceptionQuandoNaoEncontradoPorUsuarioETopico() {
        UUID usuarioId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        when(rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(usuarioId, topicoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Nenhum rascunho de canvas encontrado para o tópico informado.");
    }

    @Test
    @DisplayName("Deve retornar Optional presente ou vazio para findOptionalByUsuarioIdAndTopicoId")
    void deveRetornarOptionalParaUsuarioETopico() {
        UUID usuarioId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        RascunhoCanvas entity = RascunhoCanvas.builder().id(UUID.randomUUID()).build();

        when(rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId)).thenReturn(Optional.of(entity));

        Optional<RascunhoCanvas> optPresente = rascunhoCanvasFinder.findOptionalByUsuarioIdAndTopicoId(usuarioId, topicoId);
        assertThat(optPresente).isPresent();

        when(rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId)).thenReturn(Optional.empty());

        Optional<RascunhoCanvas> optVazio = rascunhoCanvasFinder.findOptionalByUsuarioIdAndTopicoId(usuarioId, topicoId);
        assertThat(optVazio).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar rascunhos paginados por usuario")
    void deveBuscarRascunhosPaginadosPorUsuario() {
        UUID usuarioId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        RascunhoCanvas entity = RascunhoCanvas.builder().id(UUID.randomUUID()).build();
        Page<RascunhoCanvas> pagina = new PageImpl<>(List.of(entity), pageable, 1);

        when(rascunhoCanvasRepository.findByUsuarioId(usuarioId, pageable)).thenReturn(pagina);

        Page<RascunhoCanvas> resultado = rascunhoCanvasFinder.buscarPorUsuario(usuarioId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(rascunhoCanvasRepository).findByUsuarioId(usuarioId, pageable);
    }
}
