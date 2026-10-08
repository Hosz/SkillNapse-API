package com.kyofoundation.skillnapse.modules.gamificacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.OfensivaUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfensivaUsuarioFinderTest {

    @Mock
    private OfensivaUsuarioRepository repository;

    private OfensivaUsuarioFinder finder;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        finder = new OfensivaUsuarioFinder(repository);
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
    }

    @Test
    @DisplayName("[buscarPorUsuario] Deve retornar ofensiva existente")
    void deveRetornarOfensivaExistente() {
        OfensivaUsuario ofensiva = OfensivaUsuario.builder().id(UUID.randomUUID()).usuario(usuario).build();
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(ofensiva));

        OfensivaUsuario resultado = finder.buscarPorUsuario(usuario);
        assertThat(resultado).isEqualTo(ofensiva);
    }

    @Test
    @DisplayName("[buscarPorUsuario] Deve lançar ResourceNotFoundException se não encontrar")
    void deveLancarResourceNotFoundSeNaoExistir() {
        when(repository.findByUsuario(usuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.buscarPorUsuario(usuario))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    @DisplayName("[buscarOuCriar] Deve retornar existente quando já cadastrada")
    void deveRetornarExistenteEmBuscarOuCriar() {
        OfensivaUsuario ofensiva = OfensivaUsuario.builder().id(UUID.randomUUID()).usuario(usuario).build();
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(ofensiva));

        OfensivaUsuario resultado = finder.buscarOuCriar(usuario);
        assertThat(resultado).isEqualTo(ofensiva);
    }

    @Test
    @DisplayName("[buscarOuCriar] Deve inicializar e persistir nova ofensiva quando não cadastrada")
    void deveCriarNovaOfensivaSeNaoExistir() {
        when(repository.findByUsuario(usuario)).thenReturn(Optional.empty());
        OfensivaUsuario nova = OfensivaUsuario.builder().id(UUID.randomUUID()).usuario(usuario).diasConsecutivosAtual(0).build();
        when(repository.save(any(OfensivaUsuario.class))).thenReturn(nova);

        OfensivaUsuario resultado = finder.buscarOuCriar(usuario);
        assertThat(resultado).isEqualTo(nova);
        verify(repository).save(any(OfensivaUsuario.class));
    }
}
