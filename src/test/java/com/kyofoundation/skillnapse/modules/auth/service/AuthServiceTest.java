package com.kyofoundation.skillnapse.modules.auth.service;

import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.dto.request.LoginRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.request.RegistroRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.response.LoginResponse;
import com.kyofoundation.skillnapse.modules.auth.dto.response.RegistroResponse;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.TokenAtualizacaoRepository;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import com.kyofoundation.skillnapse.modules.auth.support.JwtService;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenAtualizacaoRepository tokenAtualizacaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso e codificar senha")
    void deveRegistrarUsuarioComSucesso() {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", "senha123");

        doNothing().when(usuarioValidator).validarRegistro(request);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashSeguro");

        RegistroResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Aluno Kyo");
        assertThat(response.email()).isEqualTo("aluno@skillnapse.com");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario salvo = usuarioCaptor.getValue();
        assertThat(salvo.getNome()).isEqualTo("Aluno Kyo");
        assertThat(salvo.getEmail()).isEqualTo("aluno@skillnapse.com");
        assertThat(salvo.getSenhaHash()).isEqualTo("$2a$10$hashSeguro");
        assertThat(salvo.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve autenticar usuário com credenciais válidas e gerar Access e Refresh tokens")
    void deveRealizarLoginComSucessoEGerarTokens() {
        LoginRequest request = new LoginRequest("aluno@skillnapse.com", "senha123");
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .senhaHash("$2a$10$hashSeguro")
                .ativo(true)
                .build();

        doNothing().when(usuarioValidator).validarLogin(request);
        when(usuarioRepository.findByEmail("aluno@skillnapse.com")).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioValidator).validarCredenciais(usuario, "senha123");

        when(jwtService.gerarAccessToken(usuario)).thenReturn("mock.access.jwt");
        when(jwtService.gerarRefreshToken()).thenReturn("mock-refresh-token-uuid");
        when(jwtService.calcularExpiracaoRefreshToken()).thenReturn(Instant.now().plusSeconds(604800));
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(usuarioId);
        assertThat(response.nome()).isEqualTo("Aluno Kyo");
        assertThat(response.email()).isEqualTo("aluno@skillnapse.com");
        assertThat(response.accessToken()).isEqualTo("mock.access.jwt");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token-uuid");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);

        verify(tokenAtualizacaoRepository).save(any(TokenAtualizacao.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando email não for encontrado no login")
    void deveLancarUnauthorizedExceptionQuandoEmailNaoEncontrado() {
        LoginRequest request = new LoginRequest("inexistente@skillnapse.com", "senha123");

        doNothing().when(usuarioValidator).validarLogin(request);
        when(usuarioRepository.findByEmail("inexistente@skillnapse.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas: e-mail ou senha incorretos.");
    }

    @Test
    @DisplayName("Deve renovar token com sucesso através de rotação de refresh token")
    void deveRenovarTokenComSucesso() {
        String tokenAntigo = "refresh-token-antigo";
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .ativo(true)
                .build();

        TokenAtualizacao tokenSalvo = TokenAtualizacao.builder()
                .token(tokenAntigo)
                .usuario(usuario)
                .expiraEm(Instant.now().plusSeconds(3600))
                .revogado(false)
                .build();

        when(tokenAtualizacaoRepository.findByTokenAndRevogadoFalse(tokenAntigo)).thenReturn(Optional.of(tokenSalvo));
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);

        when(jwtService.gerarAccessToken(usuario)).thenReturn("novo.access.jwt");
        when(jwtService.gerarRefreshToken()).thenReturn("novo-refresh-token-uuid");
        when(jwtService.calcularExpiracaoRefreshToken()).thenReturn(Instant.now().plusSeconds(604800));
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.renovarToken(tokenAntigo);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("novo.access.jwt");
        assertThat(response.refreshToken()).isEqualTo("novo-refresh-token-uuid");
        assertThat(tokenSalvo.getRevogado()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao tentar renovar token expirado")
    void deveLancarUnauthorizedExceptionQuandoRefreshTokenExpirado() {
        String tokenExpirado = "refresh-token-expirado";
        TokenAtualizacao tokenSalvo = TokenAtualizacao.builder()
                .token(tokenExpirado)
                .expiraEm(Instant.now().minusSeconds(100))
                .revogado(false)
                .build();

        when(tokenAtualizacaoRepository.findByTokenAndRevogadoFalse(tokenExpirado)).thenReturn(Optional.of(tokenSalvo));

        assertThatThrownBy(() -> authService.renovarToken(tokenExpirado))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de atualização expirado.");

        assertThat(tokenSalvo.getRevogado()).isTrue();
        verify(tokenAtualizacaoRepository).save(tokenSalvo);
    }
}
