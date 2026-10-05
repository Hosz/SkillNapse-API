package com.kyofoundation.skillnapse.modules.planoestudo.repository;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicoRepository extends JpaRepository<Topico, UUID> {
    List<Topico> findByMateriaAndTopicoPaiIsNullOrderByOrdemAsc(Materia materia);
    List<Topico> findByMateriaOrderByOrdemAsc(Materia materia);
}
