package com.kyofoundation.skillnapse.modules.sessao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.sessao.dto.request.RegistrarSessaoEstudoRequest;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SessaoEstudoValidator {

    public void validarRegistro(RegistrarSessaoEstudoRequest request, Usuario usuario, Topico topico) {
        if (request == null) {
            throw new BadRequestException("Os dados da sessão de estudo não podem ser nulos.");
        }

        if (request.iniciadoEm() == null || request.finalizadoEm() == null) {
            throw new BadRequestException("Os timestamps de início e término são obrigatórios.");
        }

        if (!request.finalizadoEm().isAfter(request.iniciadoEm())) {
            throw new BadRequestException("O horário de término deve ser posterior ao horário de início.");
        }

        if (request.duracaoLiquidaSegundos() == null || request.duracaoLiquidaSegundos() <= 0) {
            throw new BadRequestException("A duração líquida deve ser maior que zero.");
        }

        long intervaloBrutoSegundos = Duration.between(request.iniciadoEm(), request.finalizadoEm()).toSeconds();
        if (request.duracaoLiquidaSegundos() > intervaloBrutoSegundos) {
            throw new BadRequestException("A duração líquida não pode ser superior ao intervalo total decorrido entre o início e o término.");
        }

        validarTopicoPertenceUsuario(usuario, topico);
    }

    public void validarTopicoPertenceUsuario(Usuario usuario, Topico topico) {
        if (topico == null ||
                topico.getMateria() == null ||
                topico.getMateria().getPlanoEstudo() == null ||
                topico.getMateria().getPlanoEstudo().getUsuario() == null ||
                !usuario.getId().equals(topico.getMateria().getPlanoEstudo().getUsuario().getId())) {
            throw new ForbiddenException("Você não tem permissão para associar sessões a este tópico.");
        }
    }

    public void validarPropriedadeSessao(Usuario usuario, SessaoEstudo sessaoEstudo) {
        if (sessaoEstudo == null || sessaoEstudo.getUsuario() == null ||
                !sessaoEstudo.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar esta sessão de estudo.");
        }
    }

    public void validarFiltroPeriodo(Instant de, Instant ate) {
        if (de != null && ate != null && de.isAfter(ate)) {
            throw new BadRequestException("A data/hora inicial ('de') não pode ser posterior à data/hora final ('ate').");
        }
    }
}
