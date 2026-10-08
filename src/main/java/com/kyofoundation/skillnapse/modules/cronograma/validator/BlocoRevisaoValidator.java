package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlocoRevisaoValidator {

    private final BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;

    public void validarCriacaoTemplate(CriarBlocoRevisaoTemplateRequest request, TemplateSemanal template, Usuario usuario) {
        if (request == null) {
            throw new BadRequestException("Os dados do bloco de revisão não podem ser nulos.");
        }
        if (!template.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("O template semanal informado não pertence ao usuário autenticado.");
        }
        if (request.diaSemana() == null) {
            throw new BadRequestException("O dia da semana é obrigatório.");
        }
        if (request.horaInicio() == null) {
            throw new BadRequestException("O horário de início é obrigatório.");
        }
        if (request.horaFim() == null) {
            throw new BadRequestException("O horário de término é obrigatório.");
        }
        validarIntervaloHorario(request.horaInicio(), request.horaFim());

        if (blocoHorarioTemplateRepository.existeSobreposicaoHorario(
                template, request.diaSemana(), request.horaInicio(), request.horaFim())) {
            throw new BadRequestException("Já existe um bloco de horário conflitante para "
                    + request.diaSemana() + " no intervalo informado.");
        }

        validarTopicoIdsRequisitados(request.topicoIds());
    }

    public void validarCriacaoDiaria(CriarBlocoRevisaoDiarioRequest request) {
        if (request == null) {
            throw new BadRequestException("Os dados da exceção de revisão não podem ser nulos.");
        }
        if (request.dataExcecao() == null) {
            throw new BadRequestException("A data da exceção diária é obrigatória.");
        }
        if (request.horaInicio() == null) {
            throw new BadRequestException("O horário de início é obrigatório.");
        }
        if (request.horaFim() == null) {
            throw new BadRequestException("O horário de término é obrigatório.");
        }
        validarIntervaloHorario(request.horaInicio(), request.horaFim());
        validarTopicoIdsRequisitados(request.topicoIds());
    }

    public void validarTopicoIdsRequisitados(List<UUID> topicoIds) {
        if (topicoIds == null || topicoIds.isEmpty()) {
            throw new BadRequestException("A lista de tópicos para revisão deve conter ao menos um tópico.");
        }
    }

    public void validarTopicosRevisao(Usuario usuario, Materia materia, List<Topico> topicos, List<UUID> topicoIdsRequisitados) {
        validarTopicoIdsRequisitados(topicoIdsRequisitados);

        Set<UUID> idsUnicos = new HashSet<>(topicoIdsRequisitados);
        if (topicos.size() != idsUnicos.size()) {
            throw new BadRequestException("Um ou mais tópicos informados não foram encontrados.");
        }

        for (Topico topico : topicos) {
            if (!topico.getMateria().getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
                throw new ForbiddenException("Um ou mais tópicos selecionados não pertencem ao usuário autenticado.");
            }
            if (materia != null && !topico.getMateria().getId().equals(materia.getId())) {
                throw new BadRequestException("O tópico '" + topico.getTitulo() + "' não pertence à matéria informada para o bloco.");
            }
        }
    }

    public void validarMateriaPertenceUsuario(Materia materia, Usuario usuario) {
        if (materia != null && !materia.getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("A matéria indicada não pertence ao usuário.");
        }
    }

    public void validarPropriedadeTemplate(BlocoHorarioTemplate bloco, Usuario usuario) {
        if (!bloco.getTemplateSemanal().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Acesso proibido a bloco de horário de outro usuário.");
        }
    }

    public void validarPropriedadeExcecao(ExcecaoDiaria excecao, Usuario usuario) {
        if (!excecao.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Acesso proibido a exceção diária de outro usuário.");
        }
    }

    public void validarTipoBlocoRevisao(TipoBloco tipoBloco) {
        if (tipoBloco != TipoBloco.REVISAO) {
            throw new BadRequestException("O bloco especificado não é do tipo REVISAO.");
        }
    }

    private void validarIntervaloHorario(LocalTime inicio, LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new BadRequestException("O horário de término deve ser posterior ao horário de início.");
        }
    }
}
