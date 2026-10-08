package com.kyofoundation.skillnapse.modules.cronograma.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.AutoAgendamentoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.finder.TemplateSemanalFinder;
import com.kyofoundation.skillnapse.modules.cronograma.mapper.AutoAgendamentoMapper;
import com.kyofoundation.skillnapse.modules.cronograma.support.AutoAgendamentoPersistenciaSupport;
import com.kyofoundation.skillnapse.modules.cronograma.support.AutoAgendamentoPromptSupport;
import com.kyofoundation.skillnapse.modules.cronograma.validator.AutoAgendamentoValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoAgendamentoService {

    private final UserFinder userFinder;
    private final UsuarioValidator usuarioValidator;
    private final PlanoEstudoFinder planoEstudoFinder;
    private final TemplateSemanalFinder templateSemanalFinder;

    private final MateriaRepository materiaRepository;
    private final TopicoRepository topicoRepository;

    private final AutoAgendamentoValidator autoAgendamentoValidator;
    private final AutoAgendamentoPromptSupport autoAgendamentoPromptSupport;
    private final AutoAgendamentoPersistenciaSupport autoAgendamentoPersistenciaSupport;
    private final AiOrchestratorService aiOrchestratorService;

    @Transactional(readOnly = true)
    public SugestaoAutoAgendamentoResponse gerarSugestao(UUID userId, GerarAutoAgendamentoRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);
        autoAgendamentoValidator.validarRequisicao(request);

        PlanoEstudo plano = planoEstudoFinder.findById(request.planoEstudoId());
        List<Materia> materias = materiaRepository.findByPlanoEstudoOrderByOrdemAsc(plano);
        autoAgendamentoValidator.validarPlano(plano, usuario, materias);

        List<Topico> topicos = topicoRepository.findByMateriaIn(materias);
        Map<UUID, Materia> mapaMaterias = materias.stream().collect(Collectors.toMap(Materia::getId, m -> m));
        Map<UUID, Topico> mapaTopicos = topicos.stream().collect(Collectors.toMap(Topico::getId, t -> t));

        PromptRequest prompt = autoAgendamentoPromptSupport.criarPrompt(plano, materias, topicos, request);
        AutoAgendamentoIaEstruturado iaResult = aiOrchestratorService.generateStructured(prompt, AutoAgendamentoIaEstruturado.class);

        return AutoAgendamentoMapper.toSugestaoResponse(plano, iaResult, mapaMaterias, mapaTopicos);
    }

    @Transactional
    public ResultadoAutoAgendamentoResponse aplicarAgendamento(UUID userId, AplicarAutoAgendamentoRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);
        autoAgendamentoValidator.validarAplicacaoRequest(request);

        PlanoEstudo plano = planoEstudoFinder.findById(request.planoEstudoId());
        List<Materia> materias = materiaRepository.findByPlanoEstudoOrderByOrdemAsc(plano);
        autoAgendamentoValidator.validarPlano(plano, usuario, materias);

        if (request.templateSemanalId() != null) {
            TemplateSemanal template = templateSemanalFinder.findById(request.templateSemanalId());
            autoAgendamentoValidator.validarTemplate(template, usuario);
        }

        List<Topico> topicos = topicoRepository.findByMateriaIn(materias);
        Map<UUID, Materia> mapaMaterias = materias.stream().collect(Collectors.toMap(Materia::getId, m -> m));
        Map<UUID, Topico> mapaTopicos = topicos.stream().collect(Collectors.toMap(Topico::getId, t -> t));

        autoAgendamentoValidator.validarBlocosParaAplicacao(request.blocos(), mapaMaterias, mapaTopicos);

        return autoAgendamentoPersistenciaSupport.persistirBlocosAprovados(usuario, request, mapaMaterias, mapaTopicos);
    }
}
