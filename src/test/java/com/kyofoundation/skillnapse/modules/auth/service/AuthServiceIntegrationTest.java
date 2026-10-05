package com.kyofoundation.skillnapse.modules.auth.service;

import com.kyofoundation.skillnapse.TestcontainersConfiguration;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.TokenAtualizacaoRepository;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenAtualizacaoRepository tokenAtualizacaoRepository;

    @Test
    @DisplayName("Deve revogar e persistir token no banco mesmo quando UnauthorizedException for lançada na expiração")
    void devePersistirRevogacaoTokenAposUnauthorizedException() {
        Usuario usuario = Usuario.builder()
                .nome("Usuario Teste")
                .email("teste.expirado." + UUID.randomUUID() + "@skillnapse.com")
                .senhaHash("$2a$10$hashSeguro")
                .ativo(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        String tokenExpirado = "token-expirado-" + UUID.randomUUID();
        TokenAtualizacao tokenAtualizacao = TokenAtualizacao.builder()
                .usuario(usuario)
                .token(tokenExpirado)
                .expiraEm(Instant.now().minusSeconds(3600))
                .revogado(false)
                .build();
        tokenAtualizacao = tokenAtualizacaoRepository.save(tokenAtualizacao);
        UUID tokenId = tokenAtualizacao.getId();

        assertThatThrownBy(() -> authService.renovarToken(tokenExpirado))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token de atualização expirado.");

        TokenAtualizacao tokenAposRenovacao = tokenAtualizacaoRepository.findById(tokenId).orElseThrow();
        assertThat(tokenAposRenovacao.getRevogado())
                .as("O token de atualização expirado deve ser marcado como revogado no banco de dados e não revertido por rollback")
                .isTrue();
    }
}
