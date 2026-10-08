package com.kyofoundation.skillnapse.modules.questao.service;

import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoDetalheResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoResumoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.finder.QuestaoFinder;
import com.kyofoundation.skillnapse.modules.questao.mapper.QuestaoMapper;
import com.kyofoundation.skillnapse.modules.questao.repository.QuestaoRepository;
import com.kyofoundation.skillnapse.modules.questao.support.HashEnunciadoSupport;
import com.kyofoundation.skillnapse.modules.questao.validator.QuestaoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final QuestaoFinder questaoFinder;
    private final QuestaoValidator questaoValidator;
    private final HashEnunciadoSupport hashEnunciadoSupport;

    @Transactional
    public QuestaoDetalheResponse criar(CriarQuestaoRequest request) {
        String hash = hashEnunciadoSupport.gerarHash(request.enunciado());
        questaoValidator.validarCriacao(request, hash);
        Questao questao = QuestaoMapper.toEntity(request, hash);
        Questao salva = questaoRepository.save(questao);
        return QuestaoMapper.toDetalheResponse(salva);
    }

    @Transactional(readOnly = true)
    public Page<QuestaoResumoResponse> buscarComFiltros(
            String assuntoGeral,
            String topicoReferencia,
            String banca,
            Integer ano,
            DificuldadeQuestao dificuldade,
            String termoBusca,
            Pageable pageable
    ) {
        Page<Questao> pagina = questaoFinder.buscarComFiltros(
                assuntoGeral,
                topicoReferencia,
                banca,
                ano,
                dificuldade,
                termoBusca,
                pageable
        );
        return pagina.map(QuestaoMapper::toResumoResponse);
    }

    @Transactional(readOnly = true)
    public QuestaoDetalheResponse buscarPorId(UUID id) {
        Questao questao = questaoFinder.findByIdComAlternativas(id);
        return QuestaoMapper.toDetalheResponse(questao);
    }
}
