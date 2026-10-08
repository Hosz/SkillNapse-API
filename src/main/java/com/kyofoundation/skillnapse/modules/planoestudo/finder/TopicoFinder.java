package com.kyofoundation.skillnapse.modules.planoestudo.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TopicoFinder {

    private final TopicoRepository topicoRepository;

    public Topico findById(UUID topicoId) {
        return topicoRepository.findById(topicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tópico não encontrado ou não existente."));
    }

    public List<Topico> findAllByIds(Collection<UUID> topicoIds) {
        if (topicoIds == null || topicoIds.isEmpty()) {
            return List.of();
        }
        return topicoRepository.findAllById(topicoIds);
    }
}
