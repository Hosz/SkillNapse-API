package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoHorarioResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TopicoRevisaoItemResponse;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlocoHorarioTemplateMapper {

    public static BlocoHorarioTemplate toCriarBlocoHorario(TemplateSemanal templateSemanal,
                                                           CriarBlocoHorarioRequest request,
                                                           Materia materia,
                                                           Topico topico) {
        TipoBloco tipo = request.tipoBloco() != null ? request.tipoBloco() : TipoBloco.FOCO_TEORIA;

        return BlocoHorarioTemplate.builder()
                .templateSemanal(templateSemanal)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .tipoBloco(tipo)
                .materia(materia)
                .topico(topico)
                .build();
    }

    public static BlocoHorarioResponse toResponse(BlocoHorarioTemplate blocoHorarioTemplate) {
        UUID materiaId = blocoHorarioTemplate.getMateria() != null ? blocoHorarioTemplate.getMateria().getId() : null;
        String materiaNome = blocoHorarioTemplate.getMateria() != null ? blocoHorarioTemplate.getMateria().getNome() : null;
        UUID topicoId = blocoHorarioTemplate.getTopico() != null ? blocoHorarioTemplate.getTopico().getId() : null;
        String topicoTitulo = blocoHorarioTemplate.getTopico() != null ? blocoHorarioTemplate.getTopico().getTitulo() : null;
        List<TopicoRevisaoItemResponse> topicosRevisao = blocoHorarioTemplate.getTopicosRevisao() != null
                ? blocoHorarioTemplate.getTopicosRevisao().stream()
                        .map(t -> new TopicoRevisaoItemResponse(
                                t.getId(),
                                t.getTitulo(),
                                t.getMateria() != null ? t.getMateria().getId() : null,
                                t.getMateria() != null ? t.getMateria().getNome() : null
                        ))
                        .toList()
                : List.of();

        return new BlocoHorarioResponse(
                blocoHorarioTemplate.getId(),
                blocoHorarioTemplate.getTemplateSemanal().getId(),
                blocoHorarioTemplate.getDiaSemana(),
                blocoHorarioTemplate.getHoraInicio(),
                blocoHorarioTemplate.getHoraFim(),
                blocoHorarioTemplate.getTipoBloco(),
                materiaId,
                materiaNome,
                topicoId,
                topicoTitulo,
                topicosRevisao
        );
    }

    public static void toEditarBlocoHorario(BlocoHorarioTemplate blocoHorarioTemplate,
                                           EditarBlocoHorarioRequest request,
                                           Materia materia,
                                           Topico topico) {
        if (request == null) {
            return;
        }
        if (request.diaSemana() != null) {
            blocoHorarioTemplate.setDiaSemana(request.diaSemana());
        }
        if (request.horaInicio() != null) {
            blocoHorarioTemplate.setHoraInicio(request.horaInicio());
        }
        if (request.horaFim() != null) {
            blocoHorarioTemplate.setHoraFim(request.horaFim());
        }
        if (request.tipoBloco() != null) {
            blocoHorarioTemplate.setTipoBloco(request.tipoBloco());
        }
        if (request.materiaId() != null) {
            blocoHorarioTemplate.setMateria(materia);
        }
        if (request.topicoId() != null) {
            blocoHorarioTemplate.setTopico(topico);
        }
    }
}
