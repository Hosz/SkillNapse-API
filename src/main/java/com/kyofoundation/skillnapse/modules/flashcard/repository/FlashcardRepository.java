package com.kyofoundation.skillnapse.modules.flashcard.repository;

import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    Page<Flashcard> findByBaralhoId(UUID baralhoId, Pageable pageable);

    List<Flashcard> findByTopicoId(UUID topicoId);

    Page<Flashcard> findByTopicoId(UUID topicoId, Pageable pageable);

    @Query("""
        SELECT f FROM Flashcard f
        JOIN f.baralho b
        WHERE b.usuario.id = :usuarioId
          AND f.proximaRevisao <= :dataReferencia
          AND (:baralhoId IS NULL OR b.id = :baralhoId)
          AND (:topicoId IS NULL OR f.topico.id = :topicoId)
        ORDER BY f.proximaRevisao ASC, f.id ASC
    """)
    Page<Flashcard> buscarCardsVencidos(
            @Param("usuarioId") UUID usuarioId,
            @Param("dataReferencia") LocalDate dataReferencia,
            @Param("baralhoId") UUID baralhoId,
            @Param("topicoId") UUID topicoId,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(f) FROM Flashcard f
        JOIN f.baralho b
        WHERE b.usuario.id = :usuarioId
          AND f.proximaRevisao <= :dataReferencia
    """)
    long contarCardsVencidos(
            @Param("usuarioId") UUID usuarioId,
            @Param("dataReferencia") LocalDate dataReferencia
    );
}
