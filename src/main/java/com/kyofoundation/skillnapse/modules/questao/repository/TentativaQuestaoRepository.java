package com.kyofoundation.skillnapse.modules.questao.repository;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TentativaQuestaoRepository extends JpaRepository<TentativaQuestao, UUID> {

    @Query("""
        SELECT COUNT(t) FROM TentativaQuestao t
        WHERE t.usuario.id = :usuarioId
          AND (CAST(:de AS instant) IS NULL OR t.respondidoEm >= :de)
          AND (CAST(:ate AS instant) IS NULL OR t.respondidoEm <= :ate)
    """)
    long contarQuestoesRespondidasNoIntervalo(
            @Param("usuarioId") UUID usuarioId,
            @Param("de") Instant de,
            @Param("ate") Instant ate
    );

    Page<TentativaQuestao> findByUsuarioOrderByRespondidoEmDesc(
            Usuario usuario,
            Pageable pageable
    );

    Page<TentativaQuestao> findByUsuarioAndAcertouOrderByRespondidoEmDesc(
            Usuario usuario,
            Boolean acertou,
            Pageable pageable
    );

    List<TentativaQuestao> findBySimuladoIdOrderByRespondidoEmAsc(UUID simuladoId);

    long countBySimuladoId(UUID simuladoId);

    long countBySimuladoIdAndAcertouTrue(UUID simuladoId);
}
