package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlocoHorarioTemplateFinder {

    private final BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;

    public Page<BlocoHorarioTemplate> findByTemplateSemanal(TemplateSemanal templateSemanal, Pageable pageable) {
        return blocoHorarioTemplateRepository.findByTemplateSemanal(templateSemanal, pageable);
    }

    public List<BlocoHorarioTemplate> findAllByTemplateSemanal(TemplateSemanal templateSemanal) {
        return blocoHorarioTemplateRepository.findAllByTemplateSemanalOrderByDiaSemanaAscHoraInicioAsc(templateSemanal);
    }

    public List<BlocoHorarioTemplate> findAllByTemplateSemanalAndDiaSemana(TemplateSemanal templateSemanal, DiaSemana diaSemana) {
        return blocoHorarioTemplateRepository.findAllByTemplateSemanalAndDiaSemanaOrderByHoraInicioAsc(templateSemanal, diaSemana);
    }

    public BlocoHorarioTemplate findById(UUID blocoId) {
        return blocoHorarioTemplateRepository.findById(blocoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloco de horário não encontrado ou não existente."));
    }
}
