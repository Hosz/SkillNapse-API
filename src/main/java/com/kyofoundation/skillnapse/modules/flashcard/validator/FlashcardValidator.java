package com.kyofoundation.skillnapse.modules.flashcard.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.RevisarFlashcardRequest;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

@Component
public class FlashcardValidator {

    public void validarCriacao(CriarFlashcardRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados de criação do flashcard não podem ser nulos.");
        }
        if (request.baralhoId() == null) {
            throw new BadRequestException("O ID do baralho é obrigatório.");
        }
        if (request.frente() == null || request.frente().isBlank()) {
            throw new BadRequestException("O texto da frente do flashcard é obrigatório.");
        }
        if (request.verso() == null || request.verso().isBlank()) {
            throw new BadRequestException("O texto do verso do flashcard é obrigatório.");
        }
    }

    public void validarAtualizacao(AtualizarFlashcardRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados de atualização do flashcard não podem ser nulos.");
        }
        if (request.frente() == null || request.frente().isBlank()) {
            throw new BadRequestException("O texto da frente do flashcard é obrigatório.");
        }
        if (request.verso() == null || request.verso().isBlank()) {
            throw new BadRequestException("O texto do verso do flashcard é obrigatório.");
        }
    }

    public void validarRevisao(RevisarFlashcardRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados da revisão do flashcard não podem ser nulos.");
        }
        if (request.classificacao() == null) {
            throw new BadRequestException("A classificação da resposta é obrigatória.");
        }
        if (request.tempoRespostaSegundos() != null && request.tempoRespostaSegundos() < 0) {
            throw new BadRequestException("O tempo de resposta não pode ser negativo.");
        }
    }

    public void validarPropriedadeFlashcard(Usuario usuario, Flashcard flashcard) {
        if (flashcard == null || flashcard.getBaralho() == null ||
                flashcard.getBaralho().getUsuario() == null ||
                !flashcard.getBaralho().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar ou modificar este flashcard.");
        }
    }

    public void validarTopicoPertenceUsuario(Usuario usuario, Topico topico) {
        if (topico != null && (topico.getMateria() == null ||
                topico.getMateria().getPlanoEstudo() == null ||
                topico.getMateria().getPlanoEstudo().getUsuario() == null ||
                !topico.getMateria().getPlanoEstudo().getUsuario().getId().equals(usuario.getId()))) {
            throw new ForbiddenException("Você não possui permissão para associar este flashcard ao tópico informado.");
        }
    }
}
