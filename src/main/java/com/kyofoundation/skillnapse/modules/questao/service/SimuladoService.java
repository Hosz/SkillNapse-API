package com.kyofoundation.skillnapse.modules.questao.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarSimuladoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.finder.SimuladoFinder;
import com.kyofoundation.skillnapse.modules.questao.finder.TentativaQuestaoFinder;
import com.kyofoundation.skillnapse.modules.questao.mapper.SimuladoMapper;
import com.kyofoundation.skillnapse.modules.questao.repository.SimuladoRepository;
import com.kyofoundation.skillnapse.modules.questao.validator.SimuladoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimuladoService {

    private final SimuladoRepository simuladoRepository;
    private final SimuladoFinder simuladoFinder;
    private final SimuladoValidator simuladoValidator;
    private final TentativaQuestaoFinder tentativaQuestaoFinder;
    private final UserFinder userFinder;

    @Transactional
    public SimuladoResponse criar(CriarSimuladoRequest request, UUID userId) {
        Usuario usuario = userFinder.findById(userId);
        simuladoValidator.validarCriacao(request);
        Simulado simulado = SimuladoMapper.toEntity(request, usuario);
        Simulado salvo = simuladoRepository.save(simulado);
        return SimuladoMapper.toResponse(salvo, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public Page<SimuladoResponse> listarPorUsuario(UUID userId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        Page<Simulado> simulados = simuladoFinder.findByUsuario(usuario, pageable);
        return simulados.map(s -> {
            long total = tentativaQuestaoFinder.contarPorSimulado(s.getId());
            long acertos = tentativaQuestaoFinder.contarAcertosPorSimulado(s.getId());
            return SimuladoMapper.toResponse(s, total, acertos);
        });
    }

    @Transactional(readOnly = true)
    public SimuladoResponse buscarPorId(UUID id, UUID userId) {
        Usuario usuario = userFinder.findById(userId);
        Simulado simulado = simuladoFinder.findById(id);
        simuladoValidator.validarPropriedade(usuario, simulado);
        long total = tentativaQuestaoFinder.contarPorSimulado(simulado.getId());
        long acertos = tentativaQuestaoFinder.contarAcertosPorSimulado(simulado.getId());
        return SimuladoMapper.toResponse(simulado, total, acertos);
    }

    @Transactional
    public SimuladoResponse concluir(UUID id, UUID userId) {
        Usuario usuario = userFinder.findById(userId);
        Simulado simulado = simuladoFinder.findById(id);
        simuladoValidator.validarPropriedade(usuario, simulado);
        simuladoValidator.validarConclusao(simulado);
        simulado.setConcluido(true);
        Simulado salvo = simuladoRepository.save(simulado);
        long total = tentativaQuestaoFinder.contarPorSimulado(salvo.getId());
        long acertos = tentativaQuestaoFinder.contarAcertosPorSimulado(salvo.getId());
        return SimuladoMapper.toResponse(salvo, total, acertos);
    }
}
