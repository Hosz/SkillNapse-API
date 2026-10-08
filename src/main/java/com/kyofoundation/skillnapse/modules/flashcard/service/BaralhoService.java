package com.kyofoundation.skillnapse.modules.flashcard.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.finder.BaralhoFinder;
import com.kyofoundation.skillnapse.modules.flashcard.mapper.BaralhoMapper;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoMetricasProjection;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoRepository;
import com.kyofoundation.skillnapse.modules.flashcard.validator.BaralhoValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaralhoService {

    private final UserFinder userFinder;
    private final MateriaFinder materiaFinder;
    private final BaralhoFinder baralhoFinder;

    private final UsuarioValidator usuarioValidator;
    private final BaralhoValidator baralhoValidator;

    private final BaralhoRepository baralhoRepository;

    @Transactional
    public BaralhoResponse criarBaralho(UUID userId, CriarBaralhoRequest request) {
        baralhoValidator.validarCriacao(request);

        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Materia materia = request.materiaId() != null ? materiaFinder.findById(request.materiaId()) : null;
        baralhoValidator.validarMateriaPertenceUsuario(usuario, materia);

        Baralho entity = BaralhoMapper.toEntity(request, usuario, materia);
        Baralho salvo = baralhoRepository.save(entity);

        return BaralhoMapper.toResponse(salvo, 0L, 0L);
    }

    @Transactional
    public BaralhoResponse atualizarBaralho(UUID userId, UUID baralhoId, AtualizarBaralhoRequest request) {
        baralhoValidator.validarAtualizacao(request);

        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Baralho baralho = baralhoFinder.findById(baralhoId);
        baralhoValidator.validarPropriedadeBaralho(usuario, baralho);

        Materia materia = request.materiaId() != null ? materiaFinder.findById(request.materiaId()) : null;
        baralhoValidator.validarMateriaPertenceUsuario(usuario, materia);

        BaralhoMapper.updateEntity(baralho, request, materia);
        Baralho salvo = baralhoRepository.save(baralho);

        Optional<BaralhoMetricasProjection> metricas = baralhoFinder.obterMetricasDoBaralho(baralhoId, LocalDate.now());
        Long totalCards = metricas.map(BaralhoMetricasProjection::getTotalCards).orElse(0L);
        Long totalCardsParaRevisar = metricas.map(BaralhoMetricasProjection::getTotalCardsParaRevisar).orElse(0L);

        return BaralhoMapper.toResponse(salvo, totalCards, totalCardsParaRevisar);
    }

    @Transactional(readOnly = true)
    public BaralhoResponse obterBaralho(UUID userId, UUID baralhoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Baralho baralho = baralhoFinder.findById(baralhoId);
        baralhoValidator.validarPropriedadeBaralho(usuario, baralho);

        Optional<BaralhoMetricasProjection> metricas = baralhoFinder.obterMetricasDoBaralho(baralhoId, LocalDate.now());
        Long totalCards = metricas.map(BaralhoMetricasProjection::getTotalCards).orElse(0L);
        Long totalCardsParaRevisar = metricas.map(BaralhoMetricasProjection::getTotalCardsParaRevisar).orElse(0L);

        return BaralhoMapper.toResponse(baralho, totalCards, totalCardsParaRevisar);
    }

    @Transactional(readOnly = true)
    public Page<BaralhoResponse> listarBaralhos(UUID userId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Page<Baralho> baralhos = baralhoFinder.buscarPorUsuario(userId, pageable);
        List<BaralhoMetricasProjection> metricas = baralhoFinder.obterMetricasPorUsuario(userId, LocalDate.now());

        Map<UUID, BaralhoMetricasProjection> metricasMap = metricas.stream()
                .collect(Collectors.toMap(BaralhoMetricasProjection::getBaralhoId, m -> m, (m1, m2) -> m1));

        return baralhos.map(b -> {
            BaralhoMetricasProjection metrica = metricasMap.get(b.getId());
            Long total = metrica != null ? metrica.getTotalCards() : 0L;
            Long aRevisar = metrica != null ? metrica.getTotalCardsParaRevisar() : 0L;
            return BaralhoMapper.toResponse(b, total, aRevisar);
        });
    }

    @Transactional
    public void apagarBaralho(UUID userId, UUID baralhoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Baralho baralho = baralhoFinder.findById(baralhoId);
        baralhoValidator.validarPropriedadeBaralho(usuario, baralho);

        baralhoRepository.delete(baralho);
    }
}
