package com.kyofoundation.skillnapse.modules.redacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.AvaliacaoCompetenciaIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import org.springframework.stereotype.Component;

@Component
public class SubmissaoRedacaoValidator {

    private static final int TAMANHO_MIN_TEXTO = 300;
    private static final int TAMANHO_MAX_TEXTO = 5000;

    public void validarRequisicao(SubmeterRedacaoRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados para submissão da redação não podem ser nulos.");
        }
        if (request.temaRedacaoId() == null) {
            throw new BadRequestException("O ID do tema de redação é obrigatório.");
        }
        if (request.textoAluno() == null || request.textoAluno().isBlank()) {
            throw new BadRequestException("O texto da redação não pode estar vazio.");
        }
        int tamanho = request.textoAluno().trim().length();
        if (tamanho < TAMANHO_MIN_TEXTO || tamanho > TAMANHO_MAX_TEXTO) {
            throw new BadRequestException("O texto da redação deve conter entre " + TAMANHO_MIN_TEXTO + " e " + TAMANHO_MAX_TEXTO + " caracteres.");
        }
    }

    public void validarPropriedadeTema(Usuario usuario, TemaRedacao tema) {
        if (tema.getPlanoEstudo() != null) {
            if (tema.getPlanoEstudo().getUsuario() == null ||
                    !tema.getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
                throw new ForbiddenException("Você não possui permissão para submeter redação para este tema.");
            }
        }
    }

    public void validarPropriedadeSubmissao(Usuario usuario, SubmissaoRedacao submissao) {
        if (submissao.getUsuario() == null || !submissao.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar esta submissão de redação.");
        }
    }

    public void validarFeedbackIa(FeedbackCorrecaoIaPayload feedback) {
        if (feedback == null) {
            throw new BadRequestException("Não foi possível obter a correção analítica da IA.");
        }
        if (feedback.notaGeral() == null || feedback.notaGeral() < 0.0 || feedback.notaGeral() > 10.0) {
            throw new BadRequestException("A nota geral da redação deve estar compreendida entre 0.0 e 10.0.");
        }
        if (feedback.competencias() == null || feedback.competencias().isEmpty()) {
            throw new BadRequestException("O feedback da IA deve conter a avaliação das competências.");
        }
        for (AvaliacaoCompetenciaIaPayload comp : feedback.competencias()) {
            if (comp.nomeCompetencia() == null || comp.nomeCompetencia().isBlank()) {
                throw new BadRequestException("A competência avaliada deve possuir um nome válido.");
            }
            if (comp.nota() == null || comp.nota() < 0.0 || comp.nota() > 10.0) {
                throw new BadRequestException("A nota de cada competência deve estar compreendida entre 0.0 e 10.0.");
            }
            if (comp.comentarios() == null || comp.comentarios().isBlank()) {
                throw new BadRequestException("A competência avaliada deve possuir comentários pedagógicos.");
            }
        }
        if (feedback.comentariosGerais() == null || feedback.comentariosGerais().isBlank()) {
            throw new BadRequestException("O parecer geral da correção da IA não pode estar vazio.");
        }
    }
}
