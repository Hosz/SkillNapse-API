package com.kyofoundation.skillnapse.modules.sessao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.sessao.dto.request.RegistrarSessaoEstudoRequest;
import com.kyofoundation.skillnapse.modules.sessao.dto.response.SessaoEstudoResponse;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessaoEstudoMapper {

    public static SessaoEstudo toEntity(RegistrarSessaoEstudoRequest request, Usuario usuario, Topico topico) {
        return SessaoEstudo.builder()
                .usuario(usuario)
                .topico(topico)
                .iniciadoEm(request.iniciadoEm())
                .finalizadoEm(request.finalizadoEm())
                .duracaoLiquidaSegundos(request.duracaoLiquidaSegundos())
                .status(request.status())
                .observacoes(request.observacoes())
                .build();
    }

    public static SessaoEstudo toRegistrarSessaoEstudo(Usuario usuario, RegistrarSessaoEstudoRequest request, Topico topico) {
        return toEntity(request, usuario, topico);
    }

    public static SessaoEstudoResponse toResponse(SessaoEstudo sessaoEstudo) {
        if (sessaoEstudo == null) {
            return null;
        }

        UUID topicoId = sessaoEstudo.getTopico() != null ? sessaoEstudo.getTopico().getId() : null;
        String topicoTitulo = sessaoEstudo.getTopico() != null ? sessaoEstudo.getTopico().getTitulo() : null;
        UUID materiaId = (sessaoEstudo.getTopico() != null && sessaoEstudo.getTopico().getMateria() != null)
                ? sessaoEstudo.getTopico().getMateria().getId() : null;
        String materiaNome = (sessaoEstudo.getTopico() != null && sessaoEstudo.getTopico().getMateria() != null)
                ? sessaoEstudo.getTopico().getMateria().getNome() : null;

        return new SessaoEstudoResponse(
                sessaoEstudo.getId(),
                topicoId,
                topicoTitulo,
                materiaId,
                materiaNome,
                sessaoEstudo.getIniciadoEm(),
                sessaoEstudo.getFinalizadoEm(),
                sessaoEstudo.getDuracaoLiquidaSegundos(),
                sessaoEstudo.getStatus(),
                sessaoEstudo.getObservacoes()
        );
    }
}
