package com.kyofoundation.skillnapse.modules.flashcard.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoMetricasProjection;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BaralhoFinder {

    private final BaralhoRepository baralhoRepository;

    public Baralho findById(UUID id) {
        return baralhoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Baralho não encontrado."));
    }

    public Page<Baralho> buscarPorUsuario(UUID usuarioId, Pageable pageable) {
        return baralhoRepository.findByUsuarioId(usuarioId, pageable);
    }

    public List<Baralho> buscarTodosPorUsuario(UUID usuarioId) {
        return baralhoRepository.findByUsuarioId(usuarioId);
    }

    public Optional<BaralhoMetricasProjection> obterMetricasDoBaralho(UUID baralhoId, LocalDate hoje) {
        return baralhoRepository.obterMetricasDoBaralho(baralhoId, hoje);
    }

    public List<BaralhoMetricasProjection> obterMetricasPorUsuario(UUID usuarioId, LocalDate hoje) {
        return baralhoRepository.obterMetricasPorUsuario(usuarioId, hoje);
    }
}
