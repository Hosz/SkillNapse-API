package com.kyofoundation.skillnapse.modules.canvas.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RascunhoCanvasValidator {

    private static final int MAX_DADOS_JSON_LENGTH = 5_000_000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public void validarSalvarRequest(SalvarRascunhoCanvasRequest request, UUID topicoIdResolvido) {
        if (request == null) {
            throw new BadRequestException("Os dados do rascunho de canvas não podem ser nulos.");
        }

        if (topicoIdResolvido == null) {
            throw new BadRequestException("O ID do tópico é obrigatório para salvar o rascunho de canvas.");
        }

        if (request.topicoId() != null && !request.topicoId().equals(topicoIdResolvido)) {
            throw new BadRequestException("O ID do tópico na URL diverge do ID do tópico informado no corpo da requisição.");
        }

        if (request.dadosDesenhoJson() == null || request.dadosDesenhoJson().isBlank()) {
            throw new BadRequestException("Os dados do desenho do canvas são obrigatórios e não podem estar em branco.");
        }

        if (request.dadosDesenhoJson().length() > MAX_DADOS_JSON_LENGTH) {
            throw new BadRequestException("O tamanho dos dados do canvas excede o limite máximo permitido de 5MB.");
        }

        try {
            OBJECT_MAPPER.readTree(request.dadosDesenhoJson());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Os dados do desenho do canvas devem ser um JSON válido.");
        }
    }

    public void validarTopicoPertenceUsuario(Usuario usuario, Topico topico) {
        if (topico == null ||
                topico.getMateria() == null ||
                topico.getMateria().getPlanoEstudo() == null ||
                topico.getMateria().getPlanoEstudo().getUsuario() == null ||
                !usuario.getId().equals(topico.getMateria().getPlanoEstudo().getUsuario().getId())) {
            throw new ForbiddenException("Você não tem permissão para associar rascunhos de canvas a este tópico.");
        }
    }

    public void validarPropriedadeCanvas(Usuario usuario, RascunhoCanvas rascunho) {
        if (rascunho == null || rascunho.getUsuario() == null ||
                !rascunho.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar este rascunho de canvas.");
        }
    }
}
