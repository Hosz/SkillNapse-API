package com.kyofoundation.skillnapse.modules.auth.mapper;

import com.kyofoundation.skillnapse.modules.auth.dto.request.RegistroRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.response.LoginResponse;
import com.kyofoundation.skillnapse.modules.auth.dto.response.RegistroResponse;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthMapper {

    public static Usuario registrar(RegistroRequest request, String senhaHash) {
        return Usuario.builder()
                .nome(request.nome())
                .email(request.email().trim().toLowerCase())
                .senhaHash(senhaHash)
                .ativo(true)
                .build();
    }

    public static RegistroResponse toRegistroResponse(Usuario usuarioRegistrado) {
        return new RegistroResponse(
                usuarioRegistrado.getId(),
                usuarioRegistrado.getNome(),
                usuarioRegistrado.getEmail()
        );
    }

    public static LoginResponse toLoginResponse(Usuario usuario, String accessToken, String refreshToken, Long expiresIn) {
        return new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn
        );
    }

    public static TokenAtualizacao toTokenAtualizacao(Usuario usuario, String token, Instant expiraEm) {
        return TokenAtualizacao.builder()
                .usuario(usuario)
                .token(token)
                .expiraEm(expiraEm)
                .revogado(false)
                .build();
    }
}
