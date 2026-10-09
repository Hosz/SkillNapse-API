package com.kyofoundation.skillnapse.modules.planoestudo.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanoEstudoRepository extends JpaRepository<PlanoEstudo, UUID> {
    Page<PlanoEstudo> findAllByUsuario(Usuario usuario, Pageable pageable);
    java.util.List<PlanoEstudo> findAllByUsuario(Usuario usuario);
}
