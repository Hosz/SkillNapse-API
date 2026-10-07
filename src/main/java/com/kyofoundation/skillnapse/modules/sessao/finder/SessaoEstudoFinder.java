package com.kyofoundation.skillnapse.modules.sessao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.repository.SessaoEstudoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessaoEstudoFinder {

    private final SessaoEstudoRepository sessaoEstudoRepository;

    public Page<SessaoEstudo> findAllByUsuario(Usuario usuario, Pageable pageable) {
        return sessaoEstudoRepository.findAllByUsuario(usuario, pageable);
    }

    public SessaoEstudo findById(UUID sessaoId) {
        return sessaoEstudoRepository.findById(sessaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada ou inexistente."));
    }

    public Page<SessaoEstudo> buscarComFiltros(UUID userId, UUID topicoId, Instant de, Instant ate, Pageable pageable) {
        return sessaoEstudoRepository.buscarComFiltros(userId, topicoId, de, ate, pageable);
    }
}

