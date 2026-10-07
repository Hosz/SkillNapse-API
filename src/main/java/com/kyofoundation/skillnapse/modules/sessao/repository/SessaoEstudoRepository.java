package com.kyofoundation.skillnapse.modules.sessao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SessaoEstudoRepository extends JpaRepository<SessaoEstudo, UUID> {

    Page<SessaoEstudo> findAllByUsuario(Usuario usuario, Pageable pageable);

    @Query("""
        SELECT s FROM SessaoEstudo s
        WHERE s.usuario.id = :usuarioId
            AND (:topicoId IS NULL OR s.topico.id = :topicoId)
            AND (CAST(:de AS instant) IS NULL OR s.iniciadoEm >= :de)
            AND (CAST(:ate AS instant) IS NULL OR s.iniciadoEm <= :ate)
    """)
    Page<SessaoEstudo> buscarComFiltros(@Param("usuarioId") UUID userId,
                                        @Param("topicoId") UUID topicoId,
                                        @Param("de") Instant de,
                                        @Param("ate") Instant ate,
                                        Pageable pageable);


    @Query("""
        SELECT
            COALESCE(SUM(s.duracaoLiquidaSegundos), 0L) AS totalSegundos,
            COALESCE(SUM(CASE WHEN s.status = 'CONCLUIDA' THEN 1L ELSE 0L END), 0L) AS totalConcluidas,
            COALESCE(SUM(CASE WHEN s.status = 'INTERROMPIDA' THEN 1L ELSE 0L END), 0L) AS totalInterrompidas
        FROM SessaoEstudo s
        WHERE s.usuario.id = :usuarioId
          AND (:topicoId IS NULL OR s.topico.id = :topicoId)
          AND (CAST(:de AS instant) IS NULL OR s.iniciadoEm >= :de)
          AND (CAST(:ate AS instant) IS NULL OR s.iniciadoEm <= :ate)
    """)
    TotalizadorSessaoProjection obterDadosAgregados(
            @Param("usuarioId") UUID userId,
            @Param("topicoId") UUID topicoId,
            @Param("de") Instant de,
            @Param("ate") Instant ate);
}

