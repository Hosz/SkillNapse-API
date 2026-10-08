package com.kyofoundation.skillnapse.modules.gamificacao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfensivaUsuarioRepository extends JpaRepository<OfensivaUsuario, UUID> {

    Optional<OfensivaUsuario> findByUsuario(Usuario usuario);

    Optional<OfensivaUsuario> findByUsuarioId(UUID usuarioId);
}
