package com.kyofoundation.skillnapse.modules.redacao.mapper;

import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.TemaRedacaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.TemaRedacaoResponse;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.TemaRedacaoResumoResponse;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.stereotype.Component;

@Component
public class TemaRedacaoMapper {

    public static TemaRedacao toEntity(TemaRedacaoIaPayload payload, PlanoEstudo plano) {
        return TemaRedacao.builder()
                .planoEstudo(plano)
                .titulo(payload.titulo().trim())
                .textosMotivadores(payload.textosMotivadores().trim())
                .criteriosAvaliacao(payload.criteriosAvaliacao() != null ? payload.criteriosAvaliacao().trim() : null)
                .geradoPorIa(true)
                .build();
    }

    public static TemaRedacaoResponse toResponse(TemaRedacao tema, long totalSubmissoes) {
        return new TemaRedacaoResponse(
                tema.getId(),
                tema.getPlanoEstudo() != null ? tema.getPlanoEstudo().getId() : null,
                tema.getTitulo(),
                tema.getTextosMotivadores(),
                tema.getCriteriosAvaliacao(),
                tema.getGeradoPorIa(),
                tema.getCriadoEm(),
                totalSubmissoes
        );
    }

    public static TemaRedacaoResumoResponse toResumoResponse(TemaRedacao tema, long totalSubmissoes) {
        return new TemaRedacaoResumoResponse(
                tema.getId(),
                tema.getPlanoEstudo() != null ? tema.getPlanoEstudo().getId() : null,
                tema.getTitulo(),
                tema.getGeradoPorIa(),
                tema.getCriadoEm(),
                totalSubmissoes
        );
    }
}
