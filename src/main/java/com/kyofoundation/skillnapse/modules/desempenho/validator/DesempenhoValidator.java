package com.kyofoundation.skillnapse.modules.desempenho.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.springframework.stereotype.Component;

@Component
public class DesempenhoValidator {

    private static final int LIMITE_MINIMO = 1;
    private static final int LIMITE_MAXIMO = 50;

    public void validarPropriedadePlano(Usuario usuario, PlanoEstudo plano) {
        if (plano == null || plano.getUsuario() == null ||
                !plano.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar os dados analíticos deste plano de estudo.");
        }
    }

    public void validarLimiteTopicosCriticos(int limite) {
        if (limite < LIMITE_MINIMO || limite > LIMITE_MAXIMO) {
            throw new BadRequestException("O limite de tópicos críticos deve ser entre " + LIMITE_MINIMO + " e " + LIMITE_MAXIMO + ".");
        }
    }
}
