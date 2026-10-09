package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.AlternativaQuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlternativaQuestaoFinder {

    private final AlternativaQuestaoRepository alternativaQuestaoRepository;

    public AlternativaQuestao findById(UUID id) {
        return alternativaQuestaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa não encontrada."));
    }

    public AlternativaQuestao findByIdAndQuestaoId(UUID id, UUID questaoId) {
        return alternativaQuestaoRepository.findByIdAndQuestaoId(id, questaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa informada não pertence à questão indicada."));
    }

    public List<AlternativaQuestao> findByQuestaoIdOrderByLetraAsc(UUID questaoId) {
        return alternativaQuestaoRepository.findByQuestaoIdOrderByLetraAsc(questaoId);
    }

    public AlternativaQuestao findCorretaPorQuestaoId(UUID questaoId) {
        return alternativaQuestaoRepository.findByQuestaoIdAndCorretaTrue(questaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa correta da questão não encontrada."));
    }
}
