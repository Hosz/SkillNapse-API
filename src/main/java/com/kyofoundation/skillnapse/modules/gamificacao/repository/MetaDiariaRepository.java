package com.kyofoundation.skillnapse.modules.gamificacao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetaDiariaRepository extends JpaRepository<MetaDiaria, UUID> {

    Optional<MetaDiaria> findByUsuario(Usuario usuario);

    Optional<MetaDiaria> findByUsuarioId(UUID usuarioId);
}
