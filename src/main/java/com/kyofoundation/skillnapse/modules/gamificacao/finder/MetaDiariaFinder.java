package com.kyofoundation.skillnapse.modules.gamificacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.MetaDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MetaDiariaFinder {

    private final MetaDiariaRepository repository;

    public MetaDiaria buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Metas diárias do usuário não encontradas."));
    }

    public MetaDiaria buscarPorUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Metas diárias do usuário não encontradas."));
    }

    public MetaDiaria buscarOuCriar(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseGet(() -> repository.save(
                        MetaDiaria.builder()
                                .usuario(usuario)
                                .metaMinutosEstudo(120)
                                .metaQuestoesResolvidas(15)
                                .build()
                ));
    }
}
