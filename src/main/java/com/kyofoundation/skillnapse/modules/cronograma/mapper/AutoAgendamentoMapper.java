package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoSugeridoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.AutoAgendamentoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.BlocoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AutoAgendamentoMapper {

    public static BlocoSugeridoResponse toBlocoSugerido(
            BlocoIaEstruturado blocoIa,
            Map<UUID, Materia> mapaMaterias,
            Map<UUID, Topico> mapaTopicos) {

        if (blocoIa == null) {
            return null;
        }

        Materia materia = blocoIa.materiaId() != null ? mapaMaterias.get(blocoIa.materiaId()) : null;
        Topico topico = blocoIa.topicoId() != null ? mapaTopicos.get(blocoIa.topicoId()) : null;

        UUID materiaIdEfetivo = materia != null ? materia.getId() : null;
        String materiaNomeEfetivo = materia != null ? materia.getNome() : null;

        UUID topicoIdEfetivo = topico != null ? topico.getId() : null;
        String topicoTituloEfetivo = topico != null ? topico.getTitulo() : null;

        return new BlocoSugeridoResponse(
                blocoIa.diaSemana(),
                blocoIa.horaInicio(),
                blocoIa.horaFim(),
                blocoIa.tipoBloco(),
                materiaIdEfetivo,
                materiaNomeEfetivo,
                topicoIdEfetivo,
                topicoTituloEfetivo,
                blocoIa.justificativaPedagogica()
        );
    }

    public static SugestaoAutoAgendamentoResponse toSugestaoResponse(
            PlanoEstudo plano,
            AutoAgendamentoIaEstruturado iaResult,
            Map<UUID, Materia> mapaMaterias,
            Map<UUID, Topico> mapaTopicos) {

        if (iaResult == null || iaResult.blocos() == null) {
            return new SugestaoAutoAgendamentoResponse(
                    plano.getId(),
                    plano.getTitulo(),
                    0.0,
                    0,
                    iaResult != null ? iaResult.resumoPedagogico() : "",
                    Collections.emptyList()
            );
        }

        List<BlocoSugeridoResponse> blocosSugeridos = iaResult.blocos().stream()
                .map(b -> toBlocoSugerido(b, mapaMaterias, mapaTopicos))
                .toList();

        double totalHoras = blocosSugeridos.stream()
                .filter(b -> b.horaInicio() != null && b.horaFim() != null)
                .mapToDouble(b -> Duration.between(b.horaInicio(), b.horaFim()).toMinutes() / 60.0)
                .sum();

        return new SugestaoAutoAgendamentoResponse(
                plano.getId(),
                plano.getTitulo(),
                Math.round(totalHoras * 10.0) / 10.0,
                blocosSugeridos.size(),
                iaResult.resumoPedagogico(),
                blocosSugeridos
        );
    }

    public static ResultadoAutoAgendamentoResponse toResultadoResponse(
            TemplateSemanal template,
            int totalBlocosPersistidos,
            String resumoPedagogico,
            List<BlocoSugeridoResponse> blocosPersistidos) {

        return new ResultadoAutoAgendamentoResponse(
                template.getId(),
                template.getNome(),
                totalBlocosPersistidos,
                resumoPedagogico,
                blocosPersistidos
        );
    }
}
