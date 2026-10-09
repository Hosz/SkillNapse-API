package com.kyofoundation.skillnapse.modules.questao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.questao.dto.response.HistoricoTentativaResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.ResultadoResolucaoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.entity.TentativaQuestao;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResolucaoQuestaoMapper {

    public static TentativaQuestao toEntity(
            Usuario usuario,
            Questao questao,
            AlternativaQuestao alternativaEscolhida,
            boolean acertou,
            Integer tempoGastoSegundos,
            Simulado simulado,
            Topico topico
    ) {
        return TentativaQuestao.builder()
                .usuario(usuario)
                .questao(questao)
                .alternativaEscolhida(alternativaEscolhida)
                .acertou(acertou)
                .tempoGastoSegundos(tempoGastoSegundos)
                .simulado(simulado)
                .topico(topico)
                .build();
    }

    public static ResultadoResolucaoResponse toResultadoResponse(
            TentativaQuestao tentativa,
            AlternativaQuestao alternativaCorreta
    ) {
        if (tentativa == null) {
            return null;
        }

        UUID questaoId = tentativa.getQuestao() != null ? tentativa.getQuestao().getId() : null;
        String explicacao = tentativa.getQuestao() != null ? tentativa.getQuestao().getExplicacaoGabarito() : null;
        UUID altEscolhidaId = tentativa.getAlternativaEscolhida() != null ? tentativa.getAlternativaEscolhida().getId() : null;
        String letraEscolhida = tentativa.getAlternativaEscolhida() != null ? tentativa.getAlternativaEscolhida().getLetra() : null;

        UUID altCorretaId = alternativaCorreta != null ? alternativaCorreta.getId() : null;
        String letraCorreta = alternativaCorreta != null ? alternativaCorreta.getLetra() : null;

        return new ResultadoResolucaoResponse(
                tentativa.getId(),
                questaoId,
                altEscolhidaId,
                letraEscolhida,
                tentativa.getAcertou(),
                altCorretaId,
                letraCorreta,
                explicacao,
                tentativa.getTempoGastoSegundos(),
                tentativa.getRespondidoEm()
        );
    }

    public static HistoricoTentativaResponse toHistoricoResponse(TentativaQuestao tentativa) {
        if (tentativa == null) {
            return null;
        }

        UUID questaoId = tentativa.getQuestao() != null ? tentativa.getQuestao().getId() : null;
        String enunciado = tentativa.getQuestao() != null ? tentativa.getQuestao().getEnunciado() : null;
        String assuntoGeral = tentativa.getQuestao() != null ? tentativa.getQuestao().getAssuntoGeral() : null;
        String topicoRef = tentativa.getQuestao() != null ? tentativa.getQuestao().getTopicoReferencia() : null;

        UUID altEscolhidaId = tentativa.getAlternativaEscolhida() != null ? tentativa.getAlternativaEscolhida().getId() : null;
        String letraEscolhida = tentativa.getAlternativaEscolhida() != null ? tentativa.getAlternativaEscolhida().getLetra() : null;
        UUID simuladoId = tentativa.getSimulado() != null ? tentativa.getSimulado().getId() : null;

        return new HistoricoTentativaResponse(
                tentativa.getId(),
                questaoId,
                enunciado,
                assuntoGeral,
                topicoRef,
                altEscolhidaId,
                letraEscolhida,
                tentativa.getAcertou(),
                tentativa.getTempoGastoSegundos(),
                simuladoId,
                tentativa.getRespondidoEm()
        );
    }
}
