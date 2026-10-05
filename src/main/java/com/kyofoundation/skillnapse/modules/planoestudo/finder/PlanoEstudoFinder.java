package com.kyofoundation.skillnapse.modules.planoestudo.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.PlanoEstudoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlanoEstudoFinder {

    private final PlanoEstudoRepository planoEstudoRepository;

    public PlanoEstudo findById(UUID planoEstudoId) {
        return planoEstudoRepository.findById(planoEstudoId)
                .orElseThrow(() -> new ResourceNotFoundException("O plano de estudo indicado não foi encontrado."));
    }
}
