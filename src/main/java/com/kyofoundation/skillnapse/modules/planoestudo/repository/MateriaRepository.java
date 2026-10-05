package com.kyofoundation.skillnapse.modules.planoestudo.repository;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MateriaRepository extends JpaRepository<Materia, UUID> {
    Page<Materia> findByPlanoEstudo(PlanoEstudo planoEstudo, Pageable pageable);
}
