package com.kyofoundation.skillnapse.modules.auth.support;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = "minha-chave-secreta-com-pelo-menos-256-bits-de-comprimento-seguro";
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        JwtEncoder jwtEncoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();

        jwtService = new JwtService(jwtEncoder, jwtDecoder);
        ReflectionTestUtils.setField(jwtService, "issuer", "https://api.skillnapse.com");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("Deve gerar access token JWT assinado contendo claims do usuário")
    void deveGerarAccessTokenComSucesso() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Hokyozu Dev")
                .email("hokyozu@skillnapse.com")
                .senhaHash("hash-seguro")
                .ativo(true)
                .build();

        String token = jwtService.gerarAccessToken(usuario);

        assertThat(token).isNotBlank();

        Jwt jwtDecodificado = jwtService.decodificarToken(token);
        assertThat(jwtDecodificado.getSubject()).isEqualTo(usuarioId.toString());
        assertThat(jwtDecodificado.getClaimAsString("nome")).isEqualTo("Hokyozu Dev");
        assertThat(jwtDecodificado.getClaimAsString("email")).isEqualTo("hokyozu@skillnapse.com");
        assertThat(jwtDecodificado.getIssuer().toString()).isEqualTo("https://api.skillnapse.com");
        assertThat(jwtDecodificado.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve extrair id do usuário e email a partir do token decodificado")
    void deveExtrairInformacoesDoToken() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .senhaHash("hash")
                .ativo(true)
                .build();

        String token = jwtService.gerarAccessToken(usuario);

        UUID idExtraido = jwtService.extrairUsuarioId(token);
        String emailExtraido = jwtService.extrairEmail(token);

        assertThat(idExtraido).isEqualTo(usuarioId);
        assertThat(emailExtraido).isEqualTo("aluno@skillnapse.com");
    }

    @Test
    @DisplayName("Deve gerar refresh token aleatório e calcular sua expiração")
    void deveGerarRefreshTokenECalcularExpiracao() {
        String refreshToken1 = jwtService.gerarRefreshToken();
        String refreshToken2 = jwtService.gerarRefreshToken();

        assertThat(refreshToken1).isNotBlank();
        assertThat(refreshToken2).isNotBlank();
        assertThat(refreshToken1).isNotEqualTo(refreshToken2);

        Instant expiracao = jwtService.calcularExpiracaoRefreshToken();
        assertThat(expiracao).isAfter(Instant.now());
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(3600L);
    }
}
