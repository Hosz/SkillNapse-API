package com.kyofoundation.skillnapse.modules.auth.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenAtualizacaoValidator {

    private static final int MAX_REFRESH_TOKEN_LENGTH = 255;

    public void validarTokenString(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("O token de atualização não pode ser nulo ou vazio.");
        }
        if (refreshToken.length() > MAX_REFRESH_TOKEN_LENGTH) {
            throw new BadRequestException("O token de atualização não pode ter mais de " + MAX_REFRESH_TOKEN_LENGTH + " caracteres.");
        }
    }

    public void validarNaoRevogado(TokenAtualizacao tokenSalvo) {
        if (Boolean.TRUE.equals(tokenSalvo.getRevogado())) {
            throw new UnauthorizedException("Tentativa de reutilização de token detectada. A sessão foi invalidada por segurança.");
        }
    }

    public void validarNaoExpirado(TokenAtualizacao tokenSalvo) {
        if (tokenSalvo.getExpiraEm().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token de atualização expirado.");
        }
    }
}
