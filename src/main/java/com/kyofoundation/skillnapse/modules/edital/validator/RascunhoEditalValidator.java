package com.kyofoundation.skillnapse.modules.edital.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.enums.StatusRascunhoEdital;
import org.springframework.stereotype.Component;

@Component
public class RascunhoEditalValidator {

    public void validarPropriedade(Usuario usuario, RascunhoEdital rascunho) {
        if (rascunho == null || rascunho.getUsuario() == null ||
                !rascunho.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar este rascunho de edital.");
        }
    }

    public void validarStatusParaEdicao(RascunhoEdital rascunho) {
        if (rascunho.getStatus() == StatusRascunhoEdital.CONVERTIDO) {
            throw new BadRequestException("Este rascunho de edital já foi convertido em um plano de estudo e não pode ser editado.");
        }
        if (rascunho.getStatus() != StatusRascunhoEdital.AGUARDANDO_APROVACAO) {
            throw new BadRequestException("Apenas rascunhos com status AGUARDANDO_APROVACAO podem ser editados.");
        }
    }

    public void validarStatusParaConversao(RascunhoEdital rascunho) {
        if (rascunho.getStatus() == StatusRascunhoEdital.CONVERTIDO) {
            throw new BadRequestException("Este rascunho de edital já foi convertido em um plano de estudo.");
        }
        if (rascunho.getStatus() != StatusRascunhoEdital.AGUARDANDO_APROVACAO) {
            throw new BadRequestException("Apenas rascunhos com status AGUARDANDO_APROVACAO podem ser convertidos em planos de estudo.");
        }
    }
}
