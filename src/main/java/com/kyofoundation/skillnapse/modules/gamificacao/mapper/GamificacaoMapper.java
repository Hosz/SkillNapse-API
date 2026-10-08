package com.kyofoundation.skillnapse.modules.gamificacao.mapper;

import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.MetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.PainelGamificacaoResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.ProgressoMetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GamificacaoMapper {

    public static StatusOfensivaResponse toStatusResponse(OfensivaUsuario ofensiva, boolean estudouHoje, boolean ofensivaAtiva) {
        if (ofensiva == null) {
            return null;
        }

        UUID usuarioId = ofensiva.getUsuario() != null ? ofensiva.getUsuario().getId() : null;
        int diasAtual = ofensiva.getDiasConsecutivosAtual() != null ? ofensiva.getDiasConsecutivosAtual() : 0;
        int maiorSeq = ofensiva.getMaiorSequenciaDias() != null ? ofensiva.getMaiorSequenciaDias() : 0;

        return new StatusOfensivaResponse(
                ofensiva.getId(),
                usuarioId,
                diasAtual,
                maiorSeq,
                ofensiva.getDataUltimoEstudo(),
                estudouHoje,
                ofensivaAtiva
        );
    }

    public static MetaDiariaResponse toMetaResponse(MetaDiaria meta) {
        if (meta == null) {
            return null;
        }

        UUID usuarioId = meta.getUsuario() != null ? meta.getUsuario().getId() : null;
        int minutos = meta.getMetaMinutosEstudo() != null ? meta.getMetaMinutosEstudo() : 120;
        int questoes = meta.getMetaQuestoesResolvidas() != null ? meta.getMetaQuestoesResolvidas() : 15;

        return new MetaDiariaResponse(
                meta.getId(),
                usuarioId,
                minutos,
                questoes
        );
    }

    public static PainelGamificacaoResponse toPainelResponse(StatusOfensivaResponse ofensiva, ProgressoMetaDiariaResponse progressoHoje) {
        return new PainelGamificacaoResponse(ofensiva, progressoHoje);
    }

    public static void aplicarAtualizacaoMeta(MetaDiaria meta, AtualizarMetaDiariaRequest request) {
        if (meta == null || request == null) {
            return;
        }
        if (request.metaMinutosEstudo() != null) {
            meta.setMetaMinutosEstudo(request.metaMinutosEstudo());
        }
        if (request.metaQuestoesResolvidas() != null) {
            meta.setMetaQuestoesResolvidas(request.metaQuestoesResolvidas());
        }
    }
}
