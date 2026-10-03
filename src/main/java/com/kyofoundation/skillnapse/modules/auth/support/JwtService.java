package com.kyofoundation.skillnapse.modules.auth.support;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${skillnapse.jwt.issuer:https://api.skillnapse.com}")
    private String issuer;

    @Value("${skillnapse.jwt.expiration:3600000}")
    private long accessTokenExpirationMs;

    @Value("${skillnapse.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plusMillis(accessTokenExpirationMs);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String gerarRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Instant calcularExpiracaoRefreshToken() {
        return Instant.now().plusMillis(refreshTokenExpirationMs);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public Jwt decodificarToken(String token) {
        return jwtDecoder.decode(token);
    }

    public UUID extrairUsuarioId(String token) {
        Jwt jwt = decodificarToken(token);
        return UUID.fromString(jwt.getSubject());
    }

    public String extrairEmail(String token) {
        Jwt jwt = decodificarToken(token);
        return jwt.getClaimAsString("email");
    }
}
