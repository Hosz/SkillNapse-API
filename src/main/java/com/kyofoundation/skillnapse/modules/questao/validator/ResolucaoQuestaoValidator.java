package com.kyofoundation.skillnapse.modules.questao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import org.springframework.stereotype.Component;

@Component
public class ResolucaoQuestaoValidator {

    public void validarResolucao(
            Questao questao,
            AlternativaQuestao alternativa,
            Integer tempoGastoSegundos,
            Simulado simulado,
            Topico topico,
            Usuario usuario
    ) {
        if (questao == null) {
            throw new BadRequestException("A questão a ser respondida não pode ser nula.");
        }

        if (alternativa == null || alternativa.getQuestao() == null ||
                !alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new BadRequestException("A alternativa informada não pertence à questão indicada.");
        }

        if (tempoGastoSegundos != null && tempoGastoSegundos < 0) {
            throw new BadRequestException("O tempo gasto deve ser maior ou igual a zero.");
        }

        if (simulado != null) {
            if (simulado.getUsuario() == null || !simulado.getUsuario().getId().equals(usuario.getId())) {
                throw new ForbiddenException("Você não possui permissão para responder questões neste simulado.");
            }
            if (Boolean.TRUE.equals(simulado.getConcluido())) {
                throw new BadRequestException("Não é possível responder questões para um simulado já concluído.");
            }
        }

        if (topico != null) {
            if (topico.getMateria() == null ||
                    topico.getMateria().getPlanoEstudo() == null ||
                    topico.getMateria().getPlanoEstudo().getUsuario() == null ||
                    !topico.getMateria().getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
                throw new ForbiddenException("Você não possui permissão para associar a resposta a este tópico.");
            }
        }
    }
}
