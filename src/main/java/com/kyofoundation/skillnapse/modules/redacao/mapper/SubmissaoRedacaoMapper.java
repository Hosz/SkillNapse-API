package com.kyofoundation.skillnapse.modules.redacao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResponse;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResumoResponse;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Component
public class SubmissaoRedacaoMapper {

    public static SubmissaoRedacao toEntity(
            SubmeterRedacaoRequest request,
            Usuario usuario,
            TemaRedacao tema,
            FeedbackCorrecaoIaPayload feedback,
            String feedbackJson,
            Instant momento
    ) {
        BigDecimal notaGeral = (feedback.notaGeral() != null)
                ? BigDecimal.valueOf(feedback.notaGeral()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return SubmissaoRedacao.builder()
                .usuario(usuario)
                .temaRedacao(tema)
                .textoAluno(request.textoAluno().trim())
                .notaGeral(notaGeral)
                .feedbackIaJson(feedbackJson)
                .corrigidoEm(momento)
                .build();
    }

    public static SubmissaoRedacaoResponse toResponse(
            SubmissaoRedacao submissao,
            FeedbackCorrecaoIaPayload feedback
    ) {
        return new SubmissaoRedacaoResponse(
                submissao.getId(),
                submissao.getTemaRedacao() != null ? submissao.getTemaRedacao().getId() : null,
                submissao.getTemaRedacao() != null ? submissao.getTemaRedacao().getTitulo() : null,
                submissao.getTextoAluno(),
                submissao.getNotaGeral(),
                feedback,
                submissao.getCorrigidoEm(),
                submissao.getCriadoEm()
        );
    }

    public static SubmissaoRedacaoResumoResponse toResumoResponse(SubmissaoRedacao submissao) {
        return new SubmissaoRedacaoResumoResponse(
                submissao.getId(),
                submissao.getTemaRedacao() != null ? submissao.getTemaRedacao().getId() : null,
                submissao.getTemaRedacao() != null ? submissao.getTemaRedacao().getTitulo() : null,
                submissao.getNotaGeral(),
                submissao.getCorrigidoEm(),
                submissao.getCriadoEm()
        );
    }
}
