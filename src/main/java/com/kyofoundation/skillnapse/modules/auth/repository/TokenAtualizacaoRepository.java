package com.kyofoundation.skillnapse.modules.auth.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.TokenAtualizacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenAtualizacaoRepository extends JpaRepository<TokenAtualizacao, UUID> {

    Optional<TokenAtualizacao> findByToken(String token);

    Optional<TokenAtualizacao> findByTokenAndRevogadoFalse(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenAtualizacao t WHERE t.token = :token")
    Optional<TokenAtualizacao> findByTokenForUpdate(@Param("token") String token);

    @Modifying
    @Query("UPDATE TokenAtualizacao t SET t.revogado = true WHERE t.usuario.id = :usuarioId AND t.revogado = false")
    void revogarTodosPorUsuario(@Param("usuarioId") UUID usuarioId);
}
