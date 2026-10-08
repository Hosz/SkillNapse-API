package com.kyofoundation.skillnapse.modules.questao.repository;

import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlternativaQuestaoRepository extends JpaRepository<AlternativaQuestao, UUID> {

    List<AlternativaQuestao> findByQuestaoIdOrderByLetraAsc(UUID questaoId);

    Optional<AlternativaQuestao> findByIdAndQuestaoId(UUID id, UUID questaoId);

    Optional<AlternativaQuestao> findByQuestaoIdAndCorretaTrue(UUID questaoId);
}
