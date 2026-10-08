package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.TentativaQuestaoRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TentativaQuestaoFinderTest {

    @Mock
    private TentativaQuestaoRepository repository;

    @InjectMocks
    private TentativaQuestaoFinder finder;

    @Test
    @DisplayName("Deve encontrar tentativa por ID")
    void deveEncontrarPorId() {
        UUID id = UUID.randomUUID();
        TentativaQuestao tentativa = TentativaQuestao.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(tentativa));

        TentativaQuestao resultado = finder.findById(id);

        assertThat(resultado).isEqualTo(tentativa);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tentativa não for encontrada")
    void deveLancarResourceNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tentativa não encontrada.");
    }

    @Test
    @DisplayName("Deve buscar tentativas por usuário sem filtro de acerto")
    void deveBuscarPorUsuarioSemFiltro() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TentativaQuestao> pagina = new PageImpl<>(List.of(TentativaQuestao.builder().id(UUID.randomUUID()).build()));

        when(repository.findByUsuarioOrderByRespondidoEmDesc(usuario, pageable)).thenReturn(pagina);

        Page<TentativaQuestao> resultado = finder.buscarPorUsuario(usuario, null, pageable);

        assertThat(resultado).isEqualTo(pagina);
    }

    @Test
    @DisplayName("Deve buscar tentativas por usuário com filtro de acerto")
    void deveBuscarPorUsuarioComFiltro() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TentativaQuestao> pagina = new PageImpl<>(List.of(TentativaQuestao.builder().id(UUID.randomUUID()).build()));

        when(repository.findByUsuarioAndAcertouOrderByRespondidoEmDesc(usuario, true, pageable)).thenReturn(pagina);

        Page<TentativaQuestao> resultado = finder.buscarPorUsuario(usuario, true, pageable);

        assertThat(resultado).isEqualTo(pagina);
    }

    @Test
    @DisplayName("Deve contar questões e acertos por simulado")
    void deveContarPorSimulado() {
        UUID simuladoId = UUID.randomUUID();

        when(repository.countBySimuladoId(simuladoId)).thenReturn(20L);
        when(repository.countBySimuladoIdAndAcertouTrue(simuladoId)).thenReturn(16L);

        assertThat(finder.contarPorSimulado(simuladoId)).isEqualTo(20L);
        assertThat(finder.contarAcertosPorSimulado(simuladoId)).isEqualTo(16L);
    }
}
