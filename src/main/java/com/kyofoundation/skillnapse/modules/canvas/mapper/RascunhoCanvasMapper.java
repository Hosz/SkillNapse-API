package com.kyofoundation.skillnapse.modules.canvas.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RascunhoCanvasMapper {

    public static RascunhoCanvas toEntity(SalvarRascunhoCanvasRequest request, Usuario usuario, Topico topico) {
        return RascunhoCanvas.builder()
                .usuario(usuario)
                .topico(topico)
                .dadosDesenhoJson(request.dadosDesenhoJson().trim())
                .atualizadoEm(Instant.now())
                .build();
    }

    public static void updateEntity(RascunhoCanvas entity, SalvarRascunhoCanvasRequest request) {
        entity.setDadosDesenhoJson(request.dadosDesenhoJson().trim());
        entity.setAtualizadoEm(Instant.now());
    }

    public static RascunhoCanvasResponse toResponse(RascunhoCanvas rascunho) {
        if (rascunho == null) {
            return null;
        }

        UUID usuarioId = rascunho.getUsuario() != null ? rascunho.getUsuario().getId() : null;
        UUID topicoId = rascunho.getTopico() != null ? rascunho.getTopico().getId() : null;
        String topicoTitulo = rascunho.getTopico() != null ? rascunho.getTopico().getTitulo() : null;

        return new RascunhoCanvasResponse(
                rascunho.getId(),
                usuarioId,
                topicoId,
                topicoTitulo,
                rascunho.getDadosDesenhoJson(),
                rascunho.getAtualizadoEm()
        );
    }
}
