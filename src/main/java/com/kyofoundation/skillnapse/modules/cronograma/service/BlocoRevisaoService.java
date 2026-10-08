package com.kyofoundation.skillnapse.modules.cronograma.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AtualizarTopicosRevisaoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.finder.BlocoRevisaoFinder;
import com.kyofoundation.skillnapse.modules.cronograma.mapper.BlocoRevisaoMapper;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
import com.kyofoundation.skillnapse.modules.cronograma.validator.BlocoRevisaoValidator;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.flashcard.repository.FlashcardRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlocoRevisaoService {

    private final BlocoRevisaoFinder blocoRevisaoFinder;
    private final BlocoRevisaoValidator blocoRevisaoValidator;
    private final BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;
    private final ExcecaoDiariaRepository excecaoDiariaRepository;
    private final FlashcardRepository flashcardRepository;

    @Transactional
    public BlocoRevisaoResponse criarBlocoRevisaoTemplate(UUID usuarioId, CriarBlocoRevisaoTemplateRequest request) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        TemplateSemanal template = blocoRevisaoFinder.buscarTemplate(request.templateSemanalId());
        blocoRevisaoValidator.validarCriacaoTemplate(request, template, usuario);

        Materia materia = blocoRevisaoFinder.buscarMateriaOpcional(request.materiaId());
        blocoRevisaoValidator.validarMateriaPertenceUsuario(materia, usuario);

        List<Topico> topicos = blocoRevisaoFinder.buscarTopicos(request.topicoIds());
        blocoRevisaoValidator.validarTopicosRevisao(usuario, materia, topicos, request.topicoIds());

        BlocoHorarioTemplate entity = BlocoRevisaoMapper.toBlocoTemplate(template, request, materia, new HashSet<>(topicos));
        BlocoHorarioTemplate salvo = blocoHorarioTemplateRepository.save(entity);

        return BlocoRevisaoMapper.toResponse(salvo);
    }

    @Transactional
    public BlocoRevisaoResponse criarBlocoRevisaoDiario(UUID usuarioId, CriarBlocoRevisaoDiarioRequest request) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        blocoRevisaoValidator.validarCriacaoDiaria(request);

        Materia materia = blocoRevisaoFinder.buscarMateriaOpcional(request.materiaId());
        blocoRevisaoValidator.validarMateriaPertenceUsuario(materia, usuario);

        List<Topico> topicos = blocoRevisaoFinder.buscarTopicos(request.topicoIds());
        blocoRevisaoValidator.validarTopicosRevisao(usuario, materia, topicos, request.topicoIds());

        ExcecaoDiaria entity = BlocoRevisaoMapper.toExcecaoDiaria(usuario, request, materia, new HashSet<>(topicos));
        ExcecaoDiaria salvo = excecaoDiariaRepository.save(entity);

        return BlocoRevisaoMapper.toResponse(salvo);
    }

    @Transactional
    public BlocoRevisaoResponse atualizarTopicosBlocoTemplate(UUID usuarioId, UUID blocoId, AtualizarTopicosRevisaoRequest request) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        BlocoHorarioTemplate bloco = blocoRevisaoFinder.buscarBlocoTemplate(blocoId);
        blocoRevisaoValidator.validarPropriedadeTemplate(bloco, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(bloco.getTipoBloco());

        List<Topico> topicos = blocoRevisaoFinder.buscarTopicos(request.topicoIds());
        blocoRevisaoValidator.validarTopicosRevisao(usuario, bloco.getMateria(), topicos, request.topicoIds());

        bloco.setTopicosRevisao(new HashSet<>(topicos));
        BlocoHorarioTemplate salvo = blocoHorarioTemplateRepository.save(bloco);

        return BlocoRevisaoMapper.toResponse(salvo);
    }

    @Transactional
    public BlocoRevisaoResponse atualizarTopicosBlocoDiario(UUID usuarioId, UUID excecaoId, AtualizarTopicosRevisaoRequest request) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        ExcecaoDiaria excecao = blocoRevisaoFinder.buscarExcecaoDiaria(excecaoId);
        blocoRevisaoValidator.validarPropriedadeExcecao(excecao, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(excecao.getTipoBloco());

        List<Topico> topicos = blocoRevisaoFinder.buscarTopicos(request.topicoIds());
        blocoRevisaoValidator.validarTopicosRevisao(usuario, excecao.getMateria(), topicos, request.topicoIds());

        excecao.setTopicosRevisao(new HashSet<>(topicos));
        ExcecaoDiaria salvo = excecaoDiariaRepository.save(excecao);

        return BlocoRevisaoMapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public BlocoRevisaoResponse visualizarBlocoRevisaoTemplate(UUID usuarioId, UUID blocoId) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        BlocoHorarioTemplate bloco = blocoRevisaoFinder.buscarBlocoTemplate(blocoId);
        blocoRevisaoValidator.validarPropriedadeTemplate(bloco, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(bloco.getTipoBloco());

        return BlocoRevisaoMapper.toResponse(bloco);
    }

    @Transactional(readOnly = true)
    public BlocoRevisaoResponse visualizarBlocoRevisaoDiario(UUID usuarioId, UUID excecaoId) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        ExcecaoDiaria excecao = blocoRevisaoFinder.buscarExcecaoDiaria(excecaoId);
        blocoRevisaoValidator.validarPropriedadeExcecao(excecao, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(excecao.getTipoBloco());

        return BlocoRevisaoMapper.toResponse(excecao);
    }

    @Transactional(readOnly = true)
    public ConteudoBlocoRevisaoResponse obterConteudoRevisaoTemplate(UUID usuarioId, UUID blocoId, LocalDate dataReferencia) {
        LocalDate dataRef = dataReferencia != null ? dataReferencia : LocalDate.now();
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        BlocoHorarioTemplate bloco = blocoRevisaoFinder.buscarBlocoTemplate(blocoId);
        blocoRevisaoValidator.validarPropriedadeTemplate(bloco, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(bloco.getTipoBloco());

        List<UUID> topicoIds = bloco.getTopicosRevisao().stream().map(Topico::getId).toList();
        List<Flashcard> cardsVencidos = topicoIds.isEmpty() ? List.of()
                : flashcardRepository.buscarCardsVencidosPorTopicos(usuarioId, topicoIds, dataRef);
        long totalCards = topicoIds.isEmpty() ? 0
                : flashcardRepository.contarCardsPorTopicos(usuarioId, topicoIds);

        return BlocoRevisaoMapper.toConteudoResponse(bloco, dataRef, cardsVencidos, totalCards);
    }

    @Transactional(readOnly = true)
    public ConteudoBlocoRevisaoResponse obterConteudoRevisaoDiario(UUID usuarioId, UUID excecaoId, LocalDate dataReferencia) {
        Usuario usuario = blocoRevisaoFinder.buscarUsuario(usuarioId);
        ExcecaoDiaria excecao = blocoRevisaoFinder.buscarExcecaoDiaria(excecaoId);
        blocoRevisaoValidator.validarPropriedadeExcecao(excecao, usuario);
        blocoRevisaoValidator.validarTipoBlocoRevisao(excecao.getTipoBloco());

        LocalDate dataRef = dataReferencia != null ? dataReferencia : excecao.getDataExcecao();
        List<UUID> topicoIds = excecao.getTopicosRevisao().stream().map(Topico::getId).toList();
        List<Flashcard> cardsVencidos = topicoIds.isEmpty() ? List.of()
                : flashcardRepository.buscarCardsVencidosPorTopicos(usuarioId, topicoIds, dataRef);
        long totalCards = topicoIds.isEmpty() ? 0
                : flashcardRepository.contarCardsPorTopicos(usuarioId, topicoIds);

        return BlocoRevisaoMapper.toConteudoResponse(excecao, dataRef, cardsVencidos, totalCards);
    }
}
