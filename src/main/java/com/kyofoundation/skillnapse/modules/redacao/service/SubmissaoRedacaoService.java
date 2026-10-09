package com.kyofoundation.skillnapse.modules.redacao.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import com.kyofoundation.skillnapse.modules.redacao.dto.request.SubmeterRedacaoRequest;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResponse;
import com.kyofoundation.skillnapse.modules.redacao.dto.response.SubmissaoRedacaoResumoResponse;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import com.kyofoundation.skillnapse.modules.redacao.finder.SubmissaoRedacaoFinder;
import com.kyofoundation.skillnapse.modules.redacao.finder.TemaRedacaoFinder;
import com.kyofoundation.skillnapse.modules.redacao.mapper.SubmissaoRedacaoMapper;
import com.kyofoundation.skillnapse.modules.redacao.repository.SubmissaoRedacaoRepository;
import com.kyofoundation.skillnapse.modules.redacao.support.JsonFeedbackRedacaoSupport;
import com.kyofoundation.skillnapse.modules.redacao.support.PromptCorrecaoRedacaoSupport;
import com.kyofoundation.skillnapse.modules.redacao.validator.SubmissaoRedacaoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissaoRedacaoService {

    private final UserFinder userFinder;
    private final UsuarioValidator usuarioValidator;
    private final TemaRedacaoFinder temaRedacaoFinder;
    private final SubmissaoRedacaoValidator submissaoRedacaoValidator;
    private final SubmissaoRedacaoFinder submissaoRedacaoFinder;
    private final SubmissaoRedacaoRepository submissaoRedacaoRepository;
    private final PromptCorrecaoRedacaoSupport promptCorrecaoRedacaoSupport;
    private final AiOrchestratorService aiOrchestratorService;
    private final JsonFeedbackRedacaoSupport jsonFeedbackRedacaoSupport;

    @Transactional
    public SubmissaoRedacaoResponse submeterRedacao(UUID userId, SubmeterRedacaoRequest request) {
        submissaoRedacaoValidator.validarRequisicao(request);

        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        TemaRedacao tema = temaRedacaoFinder.findById(request.temaRedacaoId());
        submissaoRedacaoValidator.validarPropriedadeTema(usuario, tema);

        String prompt = promptCorrecaoRedacaoSupport.construirPromptCorrecao(tema, request.textoAluno());
        PromptRequest promptRequest = new PromptRequest(prompt);
        FeedbackCorrecaoIaPayload feedback = aiOrchestratorService.generateStructured(promptRequest, FeedbackCorrecaoIaPayload.class);
        submissaoRedacaoValidator.validarFeedbackIa(feedback);

        String feedbackJson = jsonFeedbackRedacaoSupport.serializar(feedback);
        Instant momento = Instant.now();

        SubmissaoRedacao entity = SubmissaoRedacaoMapper.toEntity(
                request, usuario, tema, feedback, feedbackJson, momento
        );
        SubmissaoRedacao salva = submissaoRedacaoRepository.save(entity);

        return SubmissaoRedacaoMapper.toResponse(salva, feedback);
    }

    @Transactional(readOnly = true)
    public Page<SubmissaoRedacaoResumoResponse> listar(UUID userId, UUID temaRedacaoId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Page<SubmissaoRedacao> page;
        if (temaRedacaoId != null) {
            TemaRedacao tema = temaRedacaoFinder.findById(temaRedacaoId);
            submissaoRedacaoValidator.validarPropriedadeTema(usuario, tema);
            page = submissaoRedacaoRepository.findByUsuarioAndTemaRedacaoOrderByCriadoEmDesc(usuario, tema, pageable);
        } else {
            page = submissaoRedacaoRepository.findByUsuarioOrderByCriadoEmDesc(usuario, pageable);
        }

        return page.map(SubmissaoRedacaoMapper::toResumoResponse);
    }

    @Transactional(readOnly = true)
    public SubmissaoRedacaoResponse obterPorId(UUID userId, UUID id) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        SubmissaoRedacao submissao = submissaoRedacaoFinder.findById(id);
        submissaoRedacaoValidator.validarPropriedadeSubmissao(usuario, submissao);

        FeedbackCorrecaoIaPayload feedback = jsonFeedbackRedacaoSupport.desserializar(submissao.getFeedbackIaJson());
        return SubmissaoRedacaoMapper.toResponse(submissao, feedback);
    }
}
