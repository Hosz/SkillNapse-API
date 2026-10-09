package com.kyofoundation.skillnapse.modules.questao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.repository.SimuladoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimuladoFinder {

    private final SimuladoRepository simuladoRepository;

    public Simulado findById(UUID id) {
        return simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado não encontrado."));
    }

    public Simulado findByIdAndUsuario(UUID id, Usuario usuario) {
        return simuladoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado não encontrado."));
    }

    public Simulado findByIdAndUsuarioId(UUID id, UUID usuarioId) {
        return simuladoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado não encontrado."));
    }

    public Page<Simulado> findByUsuario(Usuario usuario, Pageable pageable) {
        return simuladoRepository.findByUsuarioOrderByCriadoEmDesc(usuario, pageable);
    }
}
