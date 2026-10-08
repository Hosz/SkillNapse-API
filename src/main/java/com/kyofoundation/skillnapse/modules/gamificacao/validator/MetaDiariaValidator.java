package com.kyofoundation.skillnapse.modules.gamificacao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import org.springframework.stereotype.Component;

@Component
public class MetaDiariaValidator {

    public void validarAtualizacao(AtualizarMetaDiariaRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados da meta diária não podem ser nulos.");
        }
        if (request.metaMinutosEstudo() == null || request.metaMinutosEstudo() <= 0) {
            throw new BadRequestException("A meta de minutos de estudo deve ser superior a zero.");
        }
        if (request.metaMinutosEstudo() > 1440) {
            throw new BadRequestException("A meta de minutos de estudo não pode exceder 24 horas (1440 minutos).");
        }
        if (request.metaQuestoesResolvidas() == null || request.metaQuestoesResolvidas() < 0) {
            throw new BadRequestException("A meta de questões resolvidas não pode ser negativa.");
        }
        if (request.metaQuestoesResolvidas() > 1000) {
            throw new BadRequestException("A meta diária de questões não pode exceder 1000 questões.");
        }
    }
}
