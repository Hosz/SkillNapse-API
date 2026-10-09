package com.kyofoundation.skillnapse.modules.redacao.repository;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TemaRedacaoRepository extends JpaRepository<TemaRedacao, UUID> {

    Page<TemaRedacao> findByPlanoEstudoOrderByCriadoEmDesc(PlanoEstudo planoEstudo, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SubmissaoRedacao s WHERE s.temaRedacao.id = :temaId")
    long countSubmissoesByTemaId(@Param("temaId") UUID temaId);
}
