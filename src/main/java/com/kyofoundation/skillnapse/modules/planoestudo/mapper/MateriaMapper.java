package com.kyofoundation.skillnapse.modules.planoestudo.mapper;

import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.MateriaResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.springframework.stereotype.Component;

@Component
public class MateriaMapper {
    public static Materia toCriacaoMateria(CriarMateriaRequest request, PlanoEstudo planoEstudo) {
        Integer ordem = 0;
        if (request.ordem() != null) {
            ordem = request.ordem();
        }
        return Materia.builder()
                .planoEstudo(planoEstudo)
                .nome(request.nome())
                .corHex(request.corHex())
                .ordem(ordem)
                .build();
    }

    public static MateriaResponse toResponse(Materia materia) {
        return new MateriaResponse(
                materia.getId(),
                materia.getNome(),
                materia.getCorHex(),
                materia.getOrdem(),
                materia.getCriadoEm()
        );
    }

    public static void toEditarMateria(Materia materia, EditarMateriaRequest request) {
        if (request.nome() != null && !request.nome().isBlank()) {
            materia.setNome(request.nome());
        }
        if (request.corHex() != null) {
            materia.setCorHex(request.corHex());
        }
        if (request.ordem() != null) {
            materia.setOrdem(request.ordem());
        }
    }
}
