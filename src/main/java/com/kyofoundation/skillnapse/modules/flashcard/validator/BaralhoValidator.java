package com.kyofoundation.skillnapse.modules.flashcard.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import org.springframework.stereotype.Component;

@Component
public class BaralhoValidator {

    private static final int MAX_TITULO_LENGTH = 150;

    public void validarCriacao(CriarBaralhoRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados de criação do baralho não podem ser nulos.");
        }
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new BadRequestException("O título do baralho é obrigatório.");
        }
        if (request.titulo().trim().length() > MAX_TITULO_LENGTH) {
            throw new BadRequestException("O título do baralho não pode ultrapassar " + MAX_TITULO_LENGTH + " caracteres.");
        }
    }

    public void validarAtualizacao(AtualizarBaralhoRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados de atualização do baralho não podem ser nulos.");
        }
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new BadRequestException("O título do baralho é obrigatório.");
        }
        if (request.titulo().trim().length() > MAX_TITULO_LENGTH) {
            throw new BadRequestException("O título do baralho não pode ultrapassar " + MAX_TITULO_LENGTH + " caracteres.");
        }
    }

    public void validarPropriedadeBaralho(Usuario usuario, Baralho baralho) {
        if (baralho == null || baralho.getUsuario() == null ||
                !baralho.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar ou modificar este baralho.");
        }
    }

    public void validarMateriaPertenceUsuario(Usuario usuario, Materia materia) {
        if (materia != null && (materia.getPlanoEstudo() == null ||
                materia.getPlanoEstudo().getUsuario() == null ||
                !materia.getPlanoEstudo().getUsuario().getId().equals(usuario.getId()))) {
            throw new ForbiddenException("Você não possui permissão para associar este baralho à matéria informada.");
        }
    }
}
