package com.kyofoundation.skillnapse.modules.auth.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFinderTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserFinder userFinder;

    @Test
    @DisplayName("Deve encontrar usuario por ID com sucesso")
    void deveEncontrarUsuarioPorIdComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).email("aluno@skillnapse.com").build();

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));

        Usuario resultado = userFinder.findById(userId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando usuario nao encontrado por ID")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorId() {
        UUID userId = UUID.randomUUID();
        when(usuarioRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não existe ou não foi encontrado.");
    }

    @Test
    @DisplayName("Deve encontrar usuario por email com sucesso")
    void deveEncontrarUsuarioPorEmailComSucesso() {
        String email = "aluno@skillnapse.com";
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).email(email).build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        Usuario resultado = userFinder.findByEmail("  ALUNO@skillnapse.com  ");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedException quando usuario nao encontrado por email")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorEmail() {
        String email = "inexistente@skillnapse.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findByEmail(email))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas: e-mail ou senha incorretos.");
    }
}
