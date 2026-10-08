package com.kyofoundation.skillnapse.modules.edital.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.repository.RascunhoEditalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RascunhoEditalFinder {

    private final RascunhoEditalRepository rascunhoEditalRepository;

    public RascunhoEdital findById(UUID rascunhoId) {
        return rascunhoEditalRepository.findById(rascunhoId)
                .orElseThrow(() -> new ResourceNotFoundException("Rascunho de edital não encontrado ou inexistente."));
    }
}
