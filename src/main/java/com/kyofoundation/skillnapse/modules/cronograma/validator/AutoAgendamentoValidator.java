package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.JanelaDisponibilidadeRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AutoAgendamentoValidator {

    public void validarRequisicao(GerarAutoAgendamentoRequest request) {
        if (request == null) {
            throw new BadRequestException("A requisição de auto-agendamento não pode ser nula.");
        }

        if (request.planoEstudoId() == null) {
            throw new BadRequestException("O identificador do plano de estudo é obrigatório.");
        }

        if (request.disponibilidades() == null || request.disponibilidades().isEmpty()) {
            throw new BadRequestException("Informe ao menos uma janela de disponibilidade semanal.");
        }

        int duracaoBloco = request.duracaoBlocoMinutosEfetiva();

        for (JanelaDisponibilidadeRequest janela : request.disponibilidades()) {
            if (janela.diaSemana() == null) {
                throw new BadRequestException("O dia da semana da janela de disponibilidade é obrigatório.");
            }
            if (janela.horaInicio() == null || janela.horaFim() == null) {
                throw new BadRequestException("Os horários de início e término da janela são obrigatórios.");
            }
            if (!janela.horaFim().isAfter(janela.horaInicio())) {
                throw new BadRequestException("O horário de término (" + janela.horaFim()
                        + ") deve ser posterior ao horário de início (" + janela.horaInicio() + ").");
            }

            long duracaoMinutos = Duration.between(janela.horaInicio(), janela.horaFim()).toMinutes();
            if (duracaoMinutos < duracaoBloco) {
                throw new BadRequestException("A janela de " + janela.diaSemana() + " ("
                        + duracaoMinutos + " min) é menor que a duração de um bloco de estudo configurado ("
                        + duracaoBloco + " min).");
            }
        }

        validarSobreposicaoInternaJanelas(request.disponibilidades());
    }

    public void validarPlano(PlanoEstudo plano, Usuario usuario, List<Materia> materias) {
        if (plano == null) {
            throw new BadRequestException("Plano de estudo inválido.");
        }
        if (!plano.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para acessar este plano de estudo.");
        }
        if (materias == null || materias.isEmpty()) {
            throw new BadRequestException("O plano de estudo '" + plano.getTitulo()
                    + "' não possui matérias cadastradas para distribuição na grade.");
        }
    }

    public void validarTemplate(TemplateSemanal template, Usuario usuario) {
        if (template != null && !template.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Você não possui permissão para modificar este template semanal.");
        }
    }

    public void validarAplicacaoRequest(AplicarAutoAgendamentoRequest request) {
        if (request == null) {
            throw new BadRequestException("A requisição de aplicação de auto-agendamento não pode ser nula.");
        }
        if (request.planoEstudoId() == null) {
            throw new BadRequestException("O identificador do plano de estudo é obrigatório.");
        }
        if (request.blocos() == null || request.blocos().isEmpty()) {
            throw new BadRequestException("Informe ao menos um bloco para aplicação no template.");
        }
    }

    public void validarBlocosParaAplicacao(
            List<BlocoParaAplicarRequest> blocos,
            Map<UUID, Materia> mapaMaterias,
            Map<UUID, Topico> mapaTopicos) {

        if (blocos == null || blocos.isEmpty()) {
            throw new BadRequestException("A lista de blocos para aplicação não pode estar vazia.");
        }

        for (BlocoParaAplicarRequest bloco : blocos) {
            if (bloco.diaSemana() == null) {
                throw new BadRequestException("O dia da semana do bloco é obrigatório.");
            }
            if (bloco.horaInicio() == null || bloco.horaFim() == null) {
                throw new BadRequestException("Os horários de início e término do bloco são obrigatórios.");
            }
            if (!bloco.horaFim().isAfter(bloco.horaInicio())) {
                throw new BadRequestException("O horário de término (" + bloco.horaFim()
                        + ") deve ser posterior ao horário de início (" + bloco.horaInicio() + ").");
            }
            if (bloco.materiaId() != null && !mapaMaterias.containsKey(bloco.materiaId())) {
                throw new BadRequestException("A matéria ID " + bloco.materiaId() + " não pertence ao plano de estudo selecionado.");
            }
            if (bloco.topicoId() != null) {
                Topico topico = mapaTopicos.get(bloco.topicoId());
                if (topico == null) {
                    throw new BadRequestException("O tópico ID " + bloco.topicoId() + " não pertence às matérias do plano de estudo.");
                }
                if (bloco.materiaId() != null && !topico.getMateria().getId().equals(bloco.materiaId())) {
                    throw new BadRequestException("O tópico ID " + bloco.topicoId() + " não pertence à matéria informada no bloco.");
                }
            }
        }

        validarSobreposicaoInternaBlocos(blocos);
    }

    private void validarSobreposicaoInternaBlocos(List<BlocoParaAplicarRequest> blocos) {
        Map<DiaSemana, List<BlocoParaAplicarRequest>> porDia = blocos.stream()
                .collect(Collectors.groupingBy(BlocoParaAplicarRequest::diaSemana));

        for (Map.Entry<DiaSemana, List<BlocoParaAplicarRequest>> entry : porDia.entrySet()) {
            List<BlocoParaAplicarRequest> listaDia = entry.getValue();
            for (int i = 0; i < listaDia.size(); i++) {
                BlocoParaAplicarRequest b1 = listaDia.get(i);
                for (int j = i + 1; j < listaDia.size(); j++) {
                    BlocoParaAplicarRequest b2 = listaDia.get(j);
                    if (b1.horaInicio().isBefore(b2.horaFim()) && b1.horaFim().isAfter(b2.horaInicio())) {
                        throw new BadRequestException("Existem blocos conflitantes/sobrepostos para "
                                + entry.getKey() + ": [" + b1.horaInicio() + " - " + b1.horaFim()
                                + "] conflita com [" + b2.horaInicio() + " - " + b2.horaFim() + "].");
                    }
                }
            }
        }
    }

    private void validarSobreposicaoInternaJanelas(List<JanelaDisponibilidadeRequest> janelas) {
        Map<DiaSemana, List<JanelaDisponibilidadeRequest>> porDia = janelas.stream()
                .collect(Collectors.groupingBy(JanelaDisponibilidadeRequest::diaSemana));

        for (Map.Entry<DiaSemana, List<JanelaDisponibilidadeRequest>> entry : porDia.entrySet()) {
            List<JanelaDisponibilidadeRequest> listaDia = entry.getValue();
            for (int i = 0; i < listaDia.size(); i++) {
                JanelaDisponibilidadeRequest j1 = listaDia.get(i);
                for (int j = i + 1; j < listaDia.size(); j++) {
                    JanelaDisponibilidadeRequest j2 = listaDia.get(j);
                    if (j1.horaInicio().isBefore(j2.horaFim()) && j1.horaFim().isAfter(j2.horaInicio())) {
                        throw new BadRequestException("Existem janelas de disponibilidade sobrepostas para "
                                + entry.getKey() + ": [" + j1.horaInicio() + " - " + j1.horaFim()
                                + "] conflita com [" + j2.horaInicio() + " - " + j2.horaFim() + "].");
                    }
                }
            }
        }
    }
}
