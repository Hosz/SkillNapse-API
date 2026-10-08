package com.kyofoundation.skillnapse.modules.canvas.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.canvas.finder.RascunhoCanvasFinder;
import com.kyofoundation.skillnapse.modules.canvas.mapper.RascunhoCanvasMapper;
import com.kyofoundation.skillnapse.modules.canvas.repository.RascunhoCanvasRepository;
import com.kyofoundation.skillnapse.modules.canvas.validator.RascunhoCanvasValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RascunhoCanvasService {

    private final UserFinder userFinder;
    private final TopicoFinder topicoFinder;
    private final RascunhoCanvasFinder rascunhoCanvasFinder;

    private final UsuarioValidator usuarioValidator;
    private final RascunhoCanvasValidator rascunhoCanvasValidator;

    private final RascunhoCanvasRepository rascunhoCanvasRepository;

    @Transactional
    public RascunhoCanvasResponse salvarRascunho(UUID userId, UUID topicoId, SalvarRascunhoCanvasRequest request) {
        UUID topicoIdResolvido = topicoId != null ? topicoId : (request != null ? request.topicoId() : null);
        rascunhoCanvasValidator.validarSalvarRequest(request, topicoIdResolvido);

        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Topico topico = topicoFinder.findById(topicoIdResolvido);
        rascunhoCanvasValidator.validarTopicoPertenceUsuario(usuario, topico);

        Optional<RascunhoCanvas> rascunhoExistente = rascunhoCanvasFinder.findOptionalByUsuarioIdAndTopicoId(userId, topicoIdResolvido);

        RascunhoCanvas entity;
        if (rascunhoExistente.isPresent()) {
            entity = rascunhoExistente.get();
            RascunhoCanvasMapper.updateEntity(entity, request);
        } else {
            entity = RascunhoCanvasMapper.toEntity(request, usuario, topico);
        }

        RascunhoCanvas salvo = rascunhoCanvasRepository.save(entity);
        return RascunhoCanvasMapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public RascunhoCanvasResponse obterRascunhoPorTopico(UUID userId, UUID topicoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Topico topico = topicoFinder.findById(topicoId);
        rascunhoCanvasValidator.validarTopicoPertenceUsuario(usuario, topico);

        RascunhoCanvas rascunho = rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(userId, topicoId);
        return RascunhoCanvasMapper.toResponse(rascunho);
    }

    @Transactional(readOnly = true)
    public RascunhoCanvasResponse obterRascunhoPorId(UUID userId, UUID canvasId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoCanvas rascunho = rascunhoCanvasFinder.findById(canvasId);
        rascunhoCanvasValidator.validarPropriedadeCanvas(usuario, rascunho);

        return RascunhoCanvasMapper.toResponse(rascunho);
    }

    @Transactional(readOnly = true)
    public Page<RascunhoCanvasResponse> listarRascunhos(UUID userId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Page<RascunhoCanvas> rascunhos = rascunhoCanvasFinder.buscarPorUsuario(userId, pageable);
        return rascunhos.map(RascunhoCanvasMapper::toResponse);
    }

    @Transactional
    public void apagarRascunhoPorTopico(UUID userId, UUID topicoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Topico topico = topicoFinder.findById(topicoId);
        rascunhoCanvasValidator.validarTopicoPertenceUsuario(usuario, topico);

        RascunhoCanvas rascunho = rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(userId, topicoId);
        rascunhoCanvasRepository.delete(rascunho);
    }

    @Transactional
    public void apagarRascunhoPorId(UUID userId, UUID canvasId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoCanvas rascunho = rascunhoCanvasFinder.findById(canvasId);
        rascunhoCanvasValidator.validarPropriedadeCanvas(usuario, rascunho);

        rascunhoCanvasRepository.delete(rascunho);
    }
}
