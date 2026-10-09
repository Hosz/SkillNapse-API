package com.kyofoundation.skillnapse.modules.edital.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.edital.dto.request.AtualizarRascunhoEditalRequest;
import com.kyofoundation.skillnapse.modules.edital.dto.request.ConverterRascunhoEditalRequest;
import com.kyofoundation.skillnapse.modules.edital.dto.response.ConversaoEditalResponse;
import com.kyofoundation.skillnapse.modules.edital.dto.response.RascunhoEditalResponse;
import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalArvoreEstruturada;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.finder.RascunhoEditalFinder;
import com.kyofoundation.skillnapse.modules.edital.mapper.EditalTreeMapper;
import com.kyofoundation.skillnapse.modules.edital.repository.RascunhoEditalRepository;
import com.kyofoundation.skillnapse.modules.edital.support.EditalConversaoSupport;
import com.kyofoundation.skillnapse.modules.edital.support.EditalPromptSupport;
import com.kyofoundation.skillnapse.modules.edital.support.PdfTextExtractorSupport;
import com.kyofoundation.skillnapse.modules.edital.validator.EditalUploadValidator;
import com.kyofoundation.skillnapse.modules.edital.validator.RascunhoEditalValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditalParserService {

    private final UserFinder userFinder;
    private final RascunhoEditalFinder rascunhoEditalFinder;

    private final UsuarioValidator usuarioValidator;
    private final EditalUploadValidator editalUploadValidator;
    private final RascunhoEditalValidator rascunhoEditalValidator;

    private final PdfTextExtractorSupport pdfTextExtractorSupport;
    private final EditalPromptSupport editalPromptSupport;
    private final EditalConversaoSupport editalConversaoSupport;

    private final AiOrchestratorService aiOrchestratorService;
    private final RascunhoEditalRepository rascunhoEditalRepository;

    @Transactional
    public RascunhoEditalResponse uploadEditalPdf(UUID userId, MultipartFile arquivo) {
        return uploadEditalPdf(userId, arquivo, null);
    }

    @Transactional
    public RascunhoEditalResponse uploadEditalPdf(UUID userId, MultipartFile arquivo, String cargoAlvo) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);
        editalUploadValidator.validarArquivo(arquivo);

        String pdfExtraido = pdfTextExtractorSupport.extrairTexto(arquivo);
        PromptRequest promptRequest = (cargoAlvo != null && !cargoAlvo.isBlank())
                ? editalPromptSupport.criarPromptParaExtracao(pdfExtraido, cargoAlvo)
                : editalPromptSupport.criarPromptParaExtracao(pdfExtraido);

        EditalArvoreEstruturada arvoreEstruturada = aiOrchestratorService
                .generateStructured(promptRequest, EditalArvoreEstruturada.class);

        String jsonString = EditalTreeMapper.toJson(arvoreEstruturada);
        RascunhoEdital rascunho = EditalTreeMapper.toEntity(arquivo.getOriginalFilename(), jsonString, usuario);
        RascunhoEdital salvo = rascunhoEditalRepository.save(rascunho);

        return EditalTreeMapper.toResponse(salvo, arvoreEstruturada);
    }

    @Transactional(readOnly = true)
    public Page<RascunhoEditalResponse> listarRascunhos(UUID userId, Pageable pageable) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        Page<RascunhoEdital> rascunhosEdital = rascunhoEditalRepository.findAllByUsuarioId(userId, pageable);
        return rascunhosEdital.map(EditalTreeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RascunhoEditalResponse visualizarRascunho(UUID userId, UUID rascunhoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoEdital rascunhoEdital = rascunhoEditalFinder.findById(rascunhoId);
        rascunhoEditalValidator.validarPropriedade(usuario, rascunhoEdital);

        return EditalTreeMapper.toResponse(rascunhoEdital);
    }

    @Transactional
    public RascunhoEditalResponse atualizarRascunho(UUID userId, UUID rascunhoId, AtualizarRascunhoEditalRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoEdital rascunhoEdital = rascunhoEditalFinder.findById(rascunhoId);
        rascunhoEditalValidator.validarPropriedade(usuario, rascunhoEdital);
        rascunhoEditalValidator.validarStatusParaEdicao(rascunhoEdital);

        String jsonString = EditalTreeMapper.toJson(request.conteudo());
        rascunhoEdital.setConteudoExtraidoJson(jsonString);
        RascunhoEdital atualizado = rascunhoEditalRepository.save(rascunhoEdital);

        return EditalTreeMapper.toResponse(atualizado, request.conteudo());
    }

    @Transactional
    public ConversaoEditalResponse converterEmPlanoEstudo(UUID userId, UUID rascunhoId, ConverterRascunhoEditalRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoEdital rascunhoEdital = rascunhoEditalFinder.findById(rascunhoId);
        rascunhoEditalValidator.validarPropriedade(usuario, rascunhoEdital);
        rascunhoEditalValidator.validarStatusParaConversao(rascunhoEdital);

        return editalConversaoSupport.executarConversao(usuario, rascunhoEdital, request);
    }

    @Transactional
    public void excluirRascunho(UUID userId, UUID rascunhoId) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        RascunhoEdital rascunhoEdital = rascunhoEditalFinder.findById(rascunhoId);
        rascunhoEditalValidator.validarPropriedade(usuario, rascunhoEdital);

        rascunhoEditalRepository.delete(rascunhoEdital);
    }
}
