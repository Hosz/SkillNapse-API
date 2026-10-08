package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.repository.TentativaQuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TentativaQuestaoFinder {

    private final TentativaQuestaoRepository tentativaQuestaoRepository;

    public TentativaQuestao findById(UUID id) {
        return tentativaQuestaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tentativa não encontrada."));
    }

    public Page<TentativaQuestao> buscarPorUsuario(Usuario usuario, Boolean acertou, Pageable pageable) {
        if (acertou != null) {
            return tentativaQuestaoRepository.findByUsuarioAndAcertouOrderByRespondidoEmDesc(usuario, acertou, pageable);
        }
        return tentativaQuestaoRepository.findByUsuarioOrderByRespondidoEmDesc(usuario, pageable);
    }

    public List<TentativaQuestao> buscarPorSimulado(UUID simuladoId) {
        return tentativaQuestaoRepository.findBySimuladoIdOrderByRespondidoEmAsc(simuladoId);
    }

    public long contarPorSimulado(UUID simuladoId) {
        return tentativaQuestaoRepository.countBySimuladoId(simuladoId);
    }

    public long contarAcertosPorSimulado(UUID simuladoId) {
        return tentativaQuestaoRepository.countBySimuladoIdAndAcertouTrue(simuladoId);
    }
}
