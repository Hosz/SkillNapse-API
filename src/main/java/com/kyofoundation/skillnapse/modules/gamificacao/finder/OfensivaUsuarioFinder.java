package com.kyofoundation.skillnapse.modules.gamificacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.OfensivaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OfensivaUsuarioFinder {

    private final OfensivaUsuarioRepository repository;

    public OfensivaUsuario buscarPorUsuario(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Ofensiva do usuário não encontrada."));
    }

    public OfensivaUsuario buscarPorUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Ofensiva do usuário não encontrada."));
    }

    public OfensivaUsuario buscarOuCriar(Usuario usuario) {
        return repository.findByUsuario(usuario)
                .orElseGet(() -> repository.save(
                        OfensivaUsuario.builder()
                                .usuario(usuario)
                                .diasConsecutivosAtual(0)
                                .maiorSequenciaDias(0)
                                .build()
                ));
    }
}
