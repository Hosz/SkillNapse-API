package com.kyofoundation.skillnapse.modules.flashcard.repository;

import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BaralhoRepository extends JpaRepository<Baralho, UUID> {

    Page<Baralho> findByUsuarioId(UUID usuarioId, Pageable pageable);

    List<Baralho> findByUsuarioId(UUID usuarioId);

    @Query("""
        SELECT b.id AS baralhoId,
               COUNT(f.id) AS totalCards,
               COUNT(CASE WHEN f.proximaRevisao <= :hoje THEN 1 END) AS totalCardsParaRevisar
        FROM Baralho b
        LEFT JOIN b.flashcards f
        WHERE b.usuario.id = :usuarioId
        GROUP BY b.id
    """)
    List<BaralhoMetricasProjection> obterMetricasPorUsuario(@Param("usuarioId") UUID usuarioId, @Param("hoje") LocalDate hoje);

    @Query("""
        SELECT b.id AS baralhoId,
               COUNT(f.id) AS totalCards,
               COUNT(CASE WHEN f.proximaRevisao <= :hoje THEN 1 END) AS totalCardsParaRevisar
        FROM Baralho b
        LEFT JOIN b.flashcards f
        WHERE b.id = :baralhoId
        GROUP BY b.id
    """)
    Optional<BaralhoMetricasProjection> obterMetricasDoBaralho(@Param("baralhoId") UUID baralhoId, @Param("hoje") LocalDate hoje);
}
