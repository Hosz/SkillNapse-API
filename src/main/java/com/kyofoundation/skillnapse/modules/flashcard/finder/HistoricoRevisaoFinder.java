package com.kyofoundation.skillnapse.modules.flashcard.finder;

import com.kyofoundation.skillnapse.modules.flashcard.entity.HistoricoRevisaoFlashcard;
import com.kyofoundation.skillnapse.modules.flashcard.repository.HistoricoRevisaoFlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HistoricoRevisaoFinder {

    private final HistoricoRevisaoFlashcardRepository historicoRepository;

    public Page<HistoricoRevisaoFlashcard> buscarPorFlashcard(UUID flashcardId, Pageable pageable) {
        return historicoRepository.findByFlashcardIdOrderByRevisadoEmDesc(flashcardId, pageable);
    }

    public Page<HistoricoRevisaoFlashcard> buscarPorUsuario(UUID usuarioId, Pageable pageable) {
        return historicoRepository.findByUsuarioIdOrderByRevisadoEmDesc(usuarioId, pageable);
    }
}
