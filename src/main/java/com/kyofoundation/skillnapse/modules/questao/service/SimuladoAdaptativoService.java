package com.kyofoundation.skillnapse.modules.questao.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.TopicoCriticoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.service.DesempenhoService;
import com.kyofoundation.skillnapse.modules.desempenho.validator.DesempenhoValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
import com.kyofoundation.skillnapse.modules.questao.dto.payload.LoteQuestoesIaPayload;
import com.kyofoundation.skillnapse.modules.questao.dto.payload.QuestaoGeradaIaPayload;
import com.kyofoundation.skillnapse.modules.questao.dto.request.GerarSimuladoAdaptativoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoItemSimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoAdaptativoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.mapper.SimuladoAdaptativoMapper;
import com.kyofoundation.skillnapse.modules.questao.repository.QuestaoRepository;
import com.kyofoundation.skillnapse.modules.questao.repository.SimuladoRepository;
import com.kyofoundation.skillnapse.modules.questao.support.HashEnunciadoSupport;
import com.kyofoundation.skillnapse.modules.questao.support.PromptQuestaoAdaptativaSupport;
import com.kyofoundation.skillnapse.modules.questao.validator.SimuladoAdaptativoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimuladoAdaptativoService {

    private final UserFinder userFinder;
    private final UsuarioValidator usuarioValidator;
    private final PlanoEstudoFinder planoEstudoFinder;
    private final DesempenhoValidator desempenhoValidator;
    private final SimuladoAdaptativoValidator simuladoAdaptativoValidator;
    private final TopicoFinder topicoFinder;
    private final TopicoRepository topicoRepository;
    private final MateriaRepository materiaRepository;
    private final DesempenhoService desempenhoService;
    private final AiOrchestratorService aiOrchestratorService;
    private final PromptQuestaoAdaptativaSupport promptQuestaoAdaptativaSupport;
    private final HashEnunciadoSupport hashEnunciadoSupport;
    private final QuestaoRepository questaoRepository;
    private final SimuladoRepository simuladoRepository;

    @Transactional
    public SimuladoAdaptativoResponse gerarSimuladoAdaptativo(UUID userId, GerarSimuladoAdaptativoRequest request) {
        Usuario usuario = userFinder.findById(userId);
        usuarioValidator.validarUsuarioAtivo(usuario);

        simuladoAdaptativoValidator.validarRequisicao(request);

        PlanoEstudo plano = planoEstudoFinder.findById(request.planoEstudoId());
        desempenhoValidator.validarPropriedadePlano(usuario, plano);

        int totalQuestoesDesejadas = request.quantidadeEfetiva();

        List<Topico> topicosAlvo = resolverTopicosAlvo(userId, plano, request, totalQuestoesDesejadas);
        simuladoAdaptativoValidator.validarTopicosPertencemPlano(topicosAlvo, plano);

        String tituloSimulado = (request.bancaAlvo() != null && !request.bancaAlvo().isBlank())
                ? "Simulado Adaptativo IA - " + request.bancaAlvo().trim()
                : "Simulado Adaptativo IA - " + plano.getTitulo();

        Simulado simulado = SimuladoAdaptativoMapper.toNovoSimulado(usuario, tituloSimulado);
        Simulado simuladoSalvo = simuladoRepository.save(simulado);

        List<QuestaoItemSimuladoResponse> questoesResponse = new ArrayList<>();
        int restante = totalQuestoesDesejadas;

        for (int i = 0; i < topicosAlvo.size() && restante > 0; i++) {
            Topico topico = topicosAlvo.get(i);
            int topicosRestantes = topicosAlvo.size() - i;
            int qtdParaEsteTopico = (restante + topicosRestantes - 1) / topicosRestantes;
            if (qtdParaEsteTopico <= 0) {
                qtdParaEsteTopico = 1;
            }
            if (qtdParaEsteTopico > restante) {
                qtdParaEsteTopico = restante;
            }

            String promptTexto = promptQuestaoAdaptativaSupport.construirPromptGeracao(
                    topico, qtdParaEsteTopico, request.bancaAlvo()
            );

            PromptRequest promptRequest = new PromptRequest(promptTexto);
            LoteQuestoesIaPayload lote = aiOrchestratorService.generateStructured(promptRequest, LoteQuestoesIaPayload.class);

            if (lote != null && lote.questoes() != null) {
                simuladoAdaptativoValidator.validarQuestoesGeradasIa(lote.questoes());

                for (QuestaoGeradaIaPayload questaoPayload : lote.questoes()) {
                    if (restante <= 0) {
                        break;
                    }
                    String hash = hashEnunciadoSupport.gerarHash(questaoPayload.enunciado());
                    Questao questao = SimuladoAdaptativoMapper.toQuestaoEntity(
                            questaoPayload, topico, hash, request.bancaAlvo()
                    );
                    Questao salva = questaoRepository.save(questao);
                    questoesResponse.add(SimuladoAdaptativoMapper.toQuestaoItemSimulado(salva, topico.getId()));
                    restante--;
                }
            }
        }

        return SimuladoAdaptativoMapper.toSimuladoAdaptativoResponse(simuladoSalvo, questoesResponse);
    }

    private List<Topico> resolverTopicosAlvo(
            UUID userId,
            PlanoEstudo plano,
            GerarSimuladoAdaptativoRequest request,
            int totalQuestoesDesejadas
    ) {
        if (request.topicoIds() != null && !request.topicoIds().isEmpty()) {
            List<Topico> topicosManuais = new ArrayList<>();
            for (UUID topicoId : request.topicoIds()) {
                topicosManuais.add(topicoFinder.findById(topicoId));
            }
            return topicosManuais;
        }

        List<TopicoCriticoResponse> criticos = desempenhoService.obterTopicosCriticos(
                userId, plano.getId(), totalQuestoesDesejadas
        );

        if (!criticos.isEmpty()) {
            List<Topico> topicosCriticos = new ArrayList<>();
            for (TopicoCriticoResponse critico : criticos) {
                topicosCriticos.add(topicoFinder.findById(critico.topicoId()));
            }
            return topicosCriticos;
        }

        List<Materia> materias = materiaRepository.findByPlanoEstudoOrderByOrdemAsc(plano);
        return topicoRepository.findByMateriaIn(materias);
    }
}
