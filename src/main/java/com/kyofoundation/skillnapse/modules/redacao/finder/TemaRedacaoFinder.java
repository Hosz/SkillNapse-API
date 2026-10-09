package com.kyofoundation.skillnapse.modules.redacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import com.kyofoundation.skillnapse.modules.redacao.repository.TemaRedacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemaRedacaoFinder {

    private final TemaRedacaoRepository temaRedacaoRepository;

    public TemaRedacao findById(UUID id) {
        return temaRedacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema de redação não encontrado com o id: " + id));
    }
}
