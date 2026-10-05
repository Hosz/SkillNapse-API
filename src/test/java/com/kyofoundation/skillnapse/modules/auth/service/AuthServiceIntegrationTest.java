package com.kyofoundation.skillnapse.modules.auth.service;

import com.kyofoundation.skillnapse.TestcontainersConfiguration;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.dto.request.LoginRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.response.LoginResponse;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.TokenAtualizacaoRepository;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import com.kyofoundation.skillnapse.modules.auth.support.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenAtualizacaoRepository tokenAtualizacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve revogar e persistir token no banco mesmo quando UnauthorizedException for lançada na expiração")
    void devePersistirRevogacaoTokenAposUnauthorizedException() {
        Usuario usuario = Usuario.builder()
                .nome("Usuario Teste")
                .email("teste.expirado." + UUID.randomUUID() + "@skillnapse.com")
                .senhaHash(passwordEncoder.encode("senha123"))
                .ativo(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        String rawTokenExpirado = "token-expirado-" + UUID.randomUUID();
        String tokenHash = jwtService.hashToken(rawTokenExpirado);

        TokenAtualizacao tokenAtualizacao = TokenAtualizacao.builder()
                .usuario(usuario)
                .token(tokenHash)
                .expiraEm(Instant.now().minusSeconds(3600))
                .revogado(false)
                .build();
        tokenAtualizacao = tokenAtualizacaoRepository.save(tokenAtualizacao);
        UUID tokenId = tokenAtualizacao.getId();

        assertThatThrownBy(() -> authService.renovarToken(rawTokenExpirado))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de atualização expirado.");

        TokenAtualizacao tokenAposRenovacao = tokenAtualizacaoRepository.findById(tokenId).orElseThrow();
        assertThat(tokenAposRenovacao.getRevogado())
                .as("O token de atualização expirado deve ser marcado como revogado no banco de dados e não revertido por rollback")
                .isTrue();
    }

    @Test
    @DisplayName("Deve armazenar apenas o hash SHA-256 do refresh token no banco e nunca o texto puro")
    void deveArmazenarApenasHashDoRefreshTokenNoBanco() {
        String email = "teste.hash." + UUID.randomUUID() + "@skillnapse.com";
        Usuario usuario = Usuario.builder()
                .nome("Usuario Hash Teste")
                .email(email)
                .senhaHash(passwordEncoder.encode("senha123"))
                .ativo(true)
                .build();
        usuarioRepository.save(usuario);

        LoginResponse loginResponse = authService.login(new LoginRequest(email, "senha123"));
        String rawRefreshToken = loginResponse.refreshToken();
        String expectedHash = jwtService.hashToken(rawRefreshToken);

        assertThat(rawRefreshToken).isNotBlank();
        assertThat(expectedHash).hasSize(64);

        TokenAtualizacao tokenNoBanco = tokenAtualizacaoRepository.findByToken(expectedHash).orElseThrow();
        assertThat(tokenNoBanco.getToken())
                .as("O token gravado no banco de dados deve ser o hash SHA-256")
                .isEqualTo(expectedHash)
                .isNotEqualTo(rawRefreshToken);

        assertThat(tokenAtualizacaoRepository.findByToken(rawRefreshToken))
                .as("Não deve existir nenhum registro no banco com o token em texto puro")
                .isEmpty();
    }

    @Test
    @DisplayName("Deve impedir reuso concorrente de refresh token garantindo que apenas uma rotação tenha sucesso")
    void deveImpedirReusoConcorrenteDeRefreshToken() throws Exception {
        String email = "teste.concorrencia." + UUID.randomUUID() + "@skillnapse.com";
        Usuario usuario = Usuario.builder()
                .nome("Usuario Concorrencia")
                .email(email)
                .senhaHash(passwordEncoder.encode("senha123"))
                .ativo(true)
                .build();
        usuarioRepository.save(usuario);

        LoginResponse loginResponse = authService.login(new LoginRequest(email, "senha123"));
        String rawRefreshToken = loginResponse.refreshToken();

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        List<LoginResponse> successResponses = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    LoginResponse response = authService.renovarToken(rawRefreshToken);
                    successResponses.add(response);
                } catch (Throwable t) {
                    exceptions.add(t);
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown(); // Dispara as duas threads simultaneamente

        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        assertThat(successResponses)
                .as("Exatamente uma chamada concorrente deve conseguir rotacionar o refresh token")
                .hasSize(1);

        assertThat(exceptions)
                .as("A outra chamada concorrente deve falhar com UnauthorizedException")
                .hasSize(1);

        assertThat(exceptions.get(0))
                .isInstanceOf(UnauthorizedException.class);
    }
}
