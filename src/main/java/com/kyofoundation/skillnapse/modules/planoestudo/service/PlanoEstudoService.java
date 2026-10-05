package com.kyofoundation.skillnapse.modules.planoestudo.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.mapper.PlanoEstudoMapper;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.PlanoEstudoRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.validator.PlanoEstudoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanoEstudoService {

    private final PlanoEstudoRepository planoEstudoRepository;

    private final PlanoEstudoValidator planoEstudoValidator;
    private final UsuarioValidator usuarioValidator;

    private final UserFinder userFinder;
    private final PlanoEstudoFinder planoEstudoFinder;

    @Transactional
    public PlanoEstudoResponse criarPlano(PlanoEstudoRequest request, UUID userId) {

        Usuario usuario = userFinder.findById(userId);

        usuarioValidator.validarUsuarioAtivo(usuario);
        planoEstudoValidator.validarCriacao(request);

        PlanoEstudo planoEstudo = PlanoEstudoMapper.criarPlano(request, usuario);
        planoEstudoRepository.save(planoEstudo);

        return PlanoEstudoMapper.toResponse(planoEstudo);
    }

    @Transactional(readOnly = true)
    public PlanoEstudoResponse visualizarPlano(UUID userId, UUID planoEstudoId) {

        Usuario usuario = userFinder.findById(userId);
        PlanoEstudo planoEstudo = planoEstudoFinder.findById(planoEstudoId);

        usuarioValidator.validarUsuarioAtivo(usuario);
        planoEstudoValidator.validarPlanoPertenceUsuario(usuario, planoEstudo);

        return PlanoEstudoMapper.toResponse(planoEstudo);
    }

    @Transactional
    public PlanoEstudoResponse editarPlano(UUID userId, UUID planoEstudoId, EdicaoPlanoEstudoRequest request) {

        Usuario usuario = userFinder.findById(userId);
        PlanoEstudo planoEstudo = planoEstudoFinder.findById(planoEstudoId);

        usuarioValidator.validarUsuarioAtivo(usuario);
        planoEstudoValidator.validarPlanoPertenceUsuario(usuario, planoEstudo);
        planoEstudoValidator.validarEdicao(request);

        PlanoEstudoMapper.toEditarPlano(planoEstudo, request);
        planoEstudoRepository.save(planoEstudo);
        return PlanoEstudoMapper.toResponse(planoEstudo);
    }

    @Transactional
    public void apagarPlano(UUID userId, UUID planoEstudoId) {
        Usuario usuario = userFinder.findById(userId);
        PlanoEstudo planoEstudo = planoEstudoFinder.findById(planoEstudoId);

        usuarioValidator.validarUsuarioAtivo(usuario);
        planoEstudoValidator.validarPlanoPertenceUsuario(usuario, planoEstudo);

        planoEstudoRepository.delete(planoEstudo);
    }

    @Transactional(readOnly = true)
    public Page<PlanoEstudoResponse> listarPlanos(UUID userId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Page<PlanoEstudo> planoEstudos = planoEstudoRepository.findAllByUsuario(usuario, pageable);
        return planoEstudos.map(PlanoEstudoMapper::toResponse);
    }
}
