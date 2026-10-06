package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoHorarioResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.GradeSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateSemanalMapper {

    public static TemplateSemanal toCriarTemplateSemanal(Usuario usuario, CriarTemplateSemanalRequest request) {
        boolean ativo = request.ativo() != null ? request.ativo() : true;

        return TemplateSemanal.builder()
                .usuario(usuario)
                .nome(request.nome().trim())
                .ativo(ativo)
                .build();
    }

    public static TemplateSemanalResponse toResponse(TemplateSemanal templateSemanal) {
        int totalBlocos = templateSemanal.getBlocosHorario() != null ? templateSemanal.getBlocosHorario().size() : 0;
        return toResponse(templateSemanal, totalBlocos);
    }

    public static TemplateSemanalResponse toResponse(TemplateSemanal templateSemanal, int totalBlocos) {
        return new TemplateSemanalResponse(
                templateSemanal.getId(),
                templateSemanal.getNome(),
                templateSemanal.getAtivo(),
                templateSemanal.getCriadoEm(),
                totalBlocos
        );
    }

    public static void toEditarTemplateSemanal(TemplateSemanal templateSemanal, EditarTemplateSemanalRequest request) {
        if (request == null) {
            return;
        }
        if (request.nome() != null && !request.nome().isBlank()) {
            templateSemanal.setNome(request.nome().trim());
        }
        if (request.ativo() != null) {
            templateSemanal.setAtivo(request.ativo());
        }
    }

    public static GradeSemanalResponse toResponseGradeSemanal(TemplateSemanal templateSemanal, List<BlocoHorarioTemplate> blocosHorario) {
        Map<DiaSemana, List<BlocoHorarioResponse>> grade = new EnumMap<>(DiaSemana.class);
        for (DiaSemana dia : DiaSemana.values()) {
            grade.put(dia, new ArrayList<>());
        }

        if (blocosHorario != null) {
            blocosHorario.stream()
                    .sorted(Comparator.comparing(BlocoHorarioTemplate::getHoraInicio))
                    .forEach(bloco -> grade.get(bloco.getDiaSemana()).add(BlocoHorarioTemplateMapper.toResponse(bloco)));
        }

        return new GradeSemanalResponse(
                templateSemanal.getId(),
                templateSemanal.getNome(),
                templateSemanal.getAtivo(),
                grade
        );
    }
}
