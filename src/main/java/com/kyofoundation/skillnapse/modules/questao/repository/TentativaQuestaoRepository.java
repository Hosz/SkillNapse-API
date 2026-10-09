package com.kyofoundation.skillnapse.modules.questao.repository;

import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TentativaQuestaoRepository extends JpaRepository<TentativaQuestao, UUID> {

    @Query("""
        SELECT COUNT(t) FROM TentativaQuestao t
        WHERE t.usuario.id = :usuarioId
          AND (CAST(:de AS instant) IS NULL OR t.respondidoEm >= :de)
          AND (CAST(:ate AS instant) IS NULL OR t.respondidoEm <= :ate)
    """)
    long contarQuestoesRespondidasNoIntervalo(
            @Param("usuarioId") UUID usuarioId,
            @Param("de") Instant de,
            @Param("ate") Instant ate
    );
}
