package com.kyofoundation.skillnapse.modules.questao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimuladoRepository extends JpaRepository<Simulado, UUID> {

    Page<Simulado> findByUsuarioOrderByCriadoEmDesc(Usuario usuario, Pageable pageable);

    Optional<Simulado> findByIdAndUsuario(UUID id, Usuario usuario);

    Optional<Simulado> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
