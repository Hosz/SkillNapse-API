package com.kyofoundation.skillnapse.modules.redacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.repository.SubmissaoRedacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubmissaoRedacaoFinder {

    private final SubmissaoRedacaoRepository submissaoRedacaoRepository;

    public SubmissaoRedacao findById(UUID id) {
        return submissaoRedacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submissão de redação não encontrada com o id: " + id));
    }
}
