package com.kyofoundation.skillnapse.modules.auth.finder;

import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.repository.TokenAtualizacaoRepository;
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
class TokenAtualizacaoFinderTest {

    @Mock
    private TokenAtualizacaoRepository tokenAtualizacaoRepository;

    @InjectMocks
    private TokenAtualizacaoFinder tokenAtualizacaoFinder;

    @Test
    @DisplayName("Deve encontrar token para atualizacao com sucesso")
    void deveEncontrarTokenParaAtualizacaoComSucesso() {
        String hash = "hash-token-123";
        TokenAtualizacao token = TokenAtualizacao.builder().id(UUID.randomUUID()).token(hash).build();

        when(tokenAtualizacaoRepository.findByTokenForUpdate(hash)).thenReturn(Optional.of(token));

        TokenAtualizacao encontrado = tokenAtualizacaoFinder.findByTokenForUpdate(hash);

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getToken()).isEqualTo(hash);
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedException quando token nao encontrado para atualizacao")
    void deveLancarUnauthorizedExceptionQuandoTokenNaoEncontradoParaAtualizacao() {
        String hash = "hash-inexistente";

        when(tokenAtualizacaoRepository.findByTokenForUpdate(hash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenAtualizacaoFinder.findByTokenForUpdate(hash))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de atualização inválido ou revogado.");
    }

    @Test
    @DisplayName("Deve buscar token simples retornando Optional")
    void deveBuscarTokenSimplesRetornandoOptional() {
        String hash = "hash-simples";
        TokenAtualizacao token = TokenAtualizacao.builder().id(UUID.randomUUID()).token(hash).build();

        when(tokenAtualizacaoRepository.findByToken(hash)).thenReturn(Optional.of(token));

        Optional<TokenAtualizacao> opt = tokenAtualizacaoFinder.findByToken(hash);

        assertThat(opt).isPresent();
        assertThat(opt.get().getToken()).isEqualTo(hash);
    }
}
