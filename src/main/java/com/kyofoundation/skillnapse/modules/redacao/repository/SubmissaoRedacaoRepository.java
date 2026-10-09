package com.kyofoundation.skillnapse.modules.redacao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubmissaoRedacaoRepository extends JpaRepository<SubmissaoRedacao, UUID> {

    Page<SubmissaoRedacao> findByUsuarioOrderByCriadoEmDesc(Usuario usuario, Pageable pageable);

    Page<SubmissaoRedacao> findByUsuarioAndTemaRedacaoOrderByCriadoEmDesc(Usuario usuario, TemaRedacao temaRedacao, Pageable pageable);

    long countByUsuario(Usuario usuario);

    long countByTemaRedacao(TemaRedacao temaRedacao);
}
