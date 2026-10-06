package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.repository.TemplateSemanalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemplateSemanalFinder {

    private final TemplateSemanalRepository templateSemanalRepository;

    public Page<TemplateSemanal> findAllByUsuario(Usuario usuario, Pageable pageable) {
        return templateSemanalRepository.findAllByUsuario(usuario, pageable);
    }

    public Page<TemplateSemanal> findAllByUsuarioAndAtivo(Usuario usuario, boolean ativo, Pageable pageable) {
        return templateSemanalRepository.findAllByUsuarioAndAtivo(usuario, ativo, pageable);
    }

    public TemplateSemanal findById(UUID templateId) {
        return templateSemanalRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template semanal não encontrado ou não existe."));
    }

    public TemplateSemanal findAtivoByUsuario(Usuario usuario) {
        return templateSemanalRepository.findByUsuarioAndAtivoTrue(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum template semanal ativo encontrado para o usuário."));
    }
}
