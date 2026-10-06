package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExcecaoDiariaFinder {

    private final ExcecaoDiariaRepository excecaoDiariaRepository;

    public ExcecaoDiaria findById(UUID excecaoId) {
        return excecaoDiariaRepository.findById(excecaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Exceção diária não encontrada ou não existente."));
    }

    public List<ExcecaoDiaria> findAllByUsuarioEData(Usuario usuario, LocalDate data) {
        return excecaoDiariaRepository.findAllByUsuarioAndDataExcecao(usuario, data);
    }
}
