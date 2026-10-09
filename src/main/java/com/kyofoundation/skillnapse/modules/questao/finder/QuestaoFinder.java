package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.QuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuestaoFinder {

    private final QuestaoRepository questaoRepository;

    public Questao findById(UUID id) {
        return questaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão não encontrada."));
    }

    public Questao findByIdComAlternativas(UUID id) {
        return questaoRepository.findByIdComAlternativas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão não encontrada."));
    }

    public Optional<Questao> findByHashEnunciado(String hashEnunciado) {
        return questaoRepository.findByHashEnunciado(hashEnunciado);
    }

    public boolean existsByHashEnunciado(String hashEnunciado) {
        return questaoRepository.existsByHashEnunciado(hashEnunciado);
    }

    public Page<Questao> buscarComFiltros(
            String assuntoGeral,
            String topicoReferencia,
            String banca,
            Integer ano,
            DificuldadeQuestao dificuldade,
            String termoBusca,
            Pageable pageable
    ) {
        return questaoRepository.buscarComFiltros(
                assuntoGeral,
                topicoReferencia,
                banca,
                ano,
                dificuldade,
                termoBusca,
                pageable
        );
    }
}
