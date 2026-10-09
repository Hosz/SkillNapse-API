package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.repository.SimuladoRepository;
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
class SimuladoFinderTest {

    @Mock
    private SimuladoRepository repository;

    @InjectMocks
    private SimuladoFinder finder;

    @Test
    @DisplayName("Deve encontrar simulado por ID")
    void deveEncontrarPorId() {
        UUID id = UUID.randomUUID();
        Simulado simulado = Simulado.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(simulado));

        Simulado resultado = finder.findById(id);

        assertThat(resultado).isEqualTo(simulado);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando simulado não existir")
    void deveLancarResourceNotFoundPorId() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Simulado não encontrado.");
    }

    @Test
    @DisplayName("Deve buscar simulados por usuário paginado")
    void deveBuscarPorUsuario() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Simulado> pagina = new PageImpl<>(List.of(Simulado.builder().id(UUID.randomUUID()).build()));

        when(repository.findByUsuarioOrderByCriadoEmDesc(usuario, pageable)).thenReturn(pagina);

        Page<Simulado> resultado = finder.findByUsuario(usuario, pageable);

        assertThat(resultado).isEqualTo(pagina);
    }
}
