package com.kyofoundation.skillnapse.modules.edital.repository;

import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RascunhoEditalRepository extends JpaRepository<RascunhoEdital, UUID> {
    Page<RascunhoEdital> findAllByUsuarioId(UUID usuarioId, Pageable pageable);
}
