package com.kyofoundation.skillnapse.modules.flashcard.repository;

import com.kyofoundation.skillnapse.modules.flashcard.entity.HistoricoRevisaoFlashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoRevisaoFlashcardRepository extends JpaRepository<HistoricoRevisaoFlashcard, UUID> {

    List<HistoricoRevisaoFlashcard> findByFlashcardIdOrderByRevisadoEmDesc(UUID flashcardId);

    Page<HistoricoRevisaoFlashcard> findByFlashcardIdOrderByRevisadoEmDesc(UUID flashcardId, Pageable pageable);

    Page<HistoricoRevisaoFlashcard> findByUsuarioIdOrderByRevisadoEmDesc(UUID usuarioId, Pageable pageable);
}
