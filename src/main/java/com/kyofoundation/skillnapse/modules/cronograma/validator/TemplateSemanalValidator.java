package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateSemanalValidator {

    private static final int MAX_NOME_LENGTH = 100;

    public void validarCriacao(CriarTemplateSemanalRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados do template semanal não podem ser nulos.");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new BadRequestException("O nome do template semanal não pode estar em branco.");
        }
        if (request.nome().trim().length() > MAX_NOME_LENGTH) {
            throw new BadRequestException("O nome do template não pode ter mais de " + MAX_NOME_LENGTH + " caracteres.");
        }
    }

    public void validarEdicao(EditarTemplateSemanalRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados de edição do template não podem ser nulos.");
        }
        boolean temNome = request.nome() != null && !request.nome().isBlank();
        boolean temAtivo = request.ativo() != null;

        if (!temNome && !temAtivo) {
            throw new BadRequestException("Pelo menos um campo deve ser informado para atualização do template.");
        }
        if (request.nome() != null) {
            if (request.nome().isBlank()) {
                throw new BadRequestException("O nome do template semanal não pode estar em branco.");
            }
            if (request.nome().trim().length() > MAX_NOME_LENGTH) {
                throw new BadRequestException("O nome do template não pode ter mais de " + MAX_NOME_LENGTH + " caracteres.");
            }
        }
    }

    public void validarTemplatePertenceUsuario(Usuario usuario, TemplateSemanal template) {
        if (!template.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("O template semanal não pertence ao usuário autenticado.");
        }
    }
}
