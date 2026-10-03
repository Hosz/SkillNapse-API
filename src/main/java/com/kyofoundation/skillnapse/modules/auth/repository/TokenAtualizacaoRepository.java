package com.kyofoundation.skillnapse.modules.auth.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenAtualizacaoRepository extends JpaRepository<TokenAtualizacao, UUID> {

    Optional<TokenAtualizacao> findByToken(String token);

    Optional<TokenAtualizacao> findByTokenAndRevogadoFalse(String token);
}
