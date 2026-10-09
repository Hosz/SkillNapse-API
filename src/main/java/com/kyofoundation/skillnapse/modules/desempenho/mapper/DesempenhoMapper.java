package com.kyofoundation.skillnapse.modules.desempenho.mapper;

import com.kyofoundation.skillnapse.modules.desempenho.dto.response.PainelGeralDesempenhoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.RelatorioLacunasResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.ResumoMateriaDesempenhoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.TopicoCriticoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.TopicoLacunaResponse;
import com.kyofoundation.skillnapse.modules.desempenho.enums.NivelCriticidadeTopico;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DesempenhoMapper {

    public static TopicoLacunaResponse toTopicoLacuna(
            Topico topico,
            Long totalTentativas,
            Long totalAcertos,
            Double taxaAcerto,
            Double tempoMedioSegundos,
            NivelCriticidadeTopico criticidade
    ) {
        return new TopicoLacunaResponse(
                topico.getId(),
                topico.getTitulo(),
                topico.getPesoEdital(),
                topico.getNivelProficiencia(),
                totalTentativas != null ? totalTentativas : 0L,
                totalAcertos != null ? totalAcertos : 0L,
                taxaAcerto != null ? taxaAcerto : 0.0,
                tempoMedioSegundos != null ? tempoMedioSegundos : 0.0,
                criticidade,
                Boolean.TRUE.equals(topico.getConcluido())
        );
    }

    public static ResumoMateriaDesempenhoResponse toResumoMateria(
            Materia materia,
            Long totalQuestoes,
            Long totalAcertos,
            Double taxaAcerto,
            Integer totalTopicosCriticos,
            List<TopicoLacunaResponse> topicos
    ) {
        return new ResumoMateriaDesempenhoResponse(
                materia.getId(),
                materia.getNome(),
                materia.getCorHex(),
                totalQuestoes != null ? totalQuestoes : 0L,
                totalAcertos != null ? totalAcertos : 0L,
                taxaAcerto != null ? taxaAcerto : 0.0,
                totalTopicosCriticos != null ? totalTopicosCriticos : 0,
                topicos != null ? topicos : List.of()
        );
    }

    public static TopicoCriticoResponse toTopicoCritico(
            UUID topicoId,
            String tituloTopico,
            UUID materiaId,
            String materiaNome,
            Integer pesoEdital,
            Double taxaAcerto,
            Long totalTentativas,
            Double indiceSeveridade,
            NivelCriticidadeTopico criticidade
    ) {
        return new TopicoCriticoResponse(
                topicoId,
                tituloTopico,
                materiaId,
                materiaNome,
                pesoEdital != null ? pesoEdital : 1,
                taxaAcerto != null ? taxaAcerto : 0.0,
                totalTentativas != null ? totalTentativas : 0L,
                indiceSeveridade != null ? indiceSeveridade : 0.0,
                criticidade
        );
    }

    public static RelatorioLacunasResponse toRelatorioLacunas(
            PlanoEstudo plano,
            Double scoreProntidao,
            Long totalQuestoesRespondidas,
            Long totalAcertos,
            Double taxaAcertoGeral,
            Integer totalTopicosCriticos,
            List<ResumoMateriaDesempenhoResponse> materias
    ) {
        return new RelatorioLacunasResponse(
                plano.getId(),
                plano.getTitulo(),
                scoreProntidao != null ? scoreProntidao : 0.0,
                totalQuestoesRespondidas != null ? totalQuestoesRespondidas : 0L,
                totalAcertos != null ? totalAcertos : 0L,
                taxaAcertoGeral != null ? taxaAcertoGeral : 0.0,
                totalTopicosCriticos != null ? totalTopicosCriticos : 0,
                materias != null ? materias : List.of()
        );
    }

    public static PainelGeralDesempenhoResponse toPainelGeral(
            Long totalQuestoesRespondidas,
            Long totalAcertos,
            Double taxaAcertoGeral,
            Double tempoMedioGeralSegundos,
            Integer totalTopicosCriticos,
            List<TopicoCriticoResponse> principaisLacunas,
            List<ResumoMateriaDesempenhoResponse> materias
    ) {
        return new PainelGeralDesempenhoResponse(
                totalQuestoesRespondidas != null ? totalQuestoesRespondidas : 0L,
                totalAcertos != null ? totalAcertos : 0L,
                taxaAcertoGeral != null ? taxaAcertoGeral : 0.0,
                tempoMedioGeralSegundos != null ? tempoMedioGeralSegundos : 0.0,
                totalTopicosCriticos != null ? totalTopicosCriticos : 0,
                principaisLacunas != null ? principaisLacunas : List.of(),
                materias != null ? materias : List.of()
        );
    }
}
