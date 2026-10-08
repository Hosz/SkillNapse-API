package com.kyofoundation.skillnapse.modules.canvas.repository;

import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RascunhoCanvasRepository extends JpaRepository<RascunhoCanvas, UUID> {

    Optional<RascunhoCanvas> findByUsuarioIdAndTopicoId(UUID usuarioId, UUID topicoId);

    boolean existsByUsuarioIdAndTopicoId(UUID usuarioId, UUID topicoId);

    Page<RascunhoCanvas> findByUsuarioId(UUID usuarioId, Pageable pageable);

    void deleteByUsuarioIdAndTopicoId(UUID usuarioId, UUID topicoId);
}
