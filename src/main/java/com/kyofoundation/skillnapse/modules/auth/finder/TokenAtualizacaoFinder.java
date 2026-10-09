package com.kyofoundation.skillnapse.modules.auth.finder;

import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.repository.TokenAtualizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenAtualizacaoFinder {

    private final TokenAtualizacaoRepository tokenAtualizacaoRepository;

    public TokenAtualizacao findByTokenForUpdate(String tokenHash) {
        return tokenAtualizacaoRepository.findByTokenForUpdate(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Token de atualização inválido ou revogado."));
    }

    public Optional<TokenAtualizacao> findByToken(String tokenHash) {
        return tokenAtualizacaoRepository.findByToken(tokenHash);
    }
}
