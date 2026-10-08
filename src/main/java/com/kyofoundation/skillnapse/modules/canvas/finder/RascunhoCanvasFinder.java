package com.kyofoundation.skillnapse.modules.canvas.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.canvas.repository.RascunhoCanvasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RascunhoCanvasFinder {

    private final RascunhoCanvasRepository rascunhoCanvasRepository;

    public RascunhoCanvas findById(UUID id) {
        return rascunhoCanvasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rascunho de canvas não encontrado."));
    }

    public RascunhoCanvas findByUsuarioIdAndTopicoId(UUID usuarioId, UUID topicoId) {
        return rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum rascunho de canvas encontrado para o tópico informado."));
    }

    public Optional<RascunhoCanvas> findOptionalByUsuarioIdAndTopicoId(UUID usuarioId, UUID topicoId) {
        return rascunhoCanvasRepository.findByUsuarioIdAndTopicoId(usuarioId, topicoId);
    }

    public Page<RascunhoCanvas> buscarPorUsuario(UUID usuarioId, Pageable pageable) {
        return rascunhoCanvasRepository.findByUsuarioId(usuarioId, pageable);
    }
}
