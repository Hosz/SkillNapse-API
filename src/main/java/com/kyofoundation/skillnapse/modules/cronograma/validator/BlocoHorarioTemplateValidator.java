package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BlocoHorarioTemplateValidator {

    private final BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;

    public void validarCriacao(CriarBlocoHorarioRequest request, TemplateSemanal template) {
        if (request == null) {
            throw new BadRequestException("Os dados do bloco de horário não podem ser nulos.");
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
    }

    public void validarEdicao(EditarBlocoHorarioRequest request, BlocoHorarioTemplate blocoExistente, TemplateSemanal template) {
        if (request == null) {
            throw new BadRequestException("Os dados de edição do bloco de horário não podem ser nulos.");
        }
        DiaSemana diaSemanaEfetivo = request.diaSemana() != null ? request.diaSemana() : blocoExistente.getDiaSemana();
        LocalTime horaInicioEfetiva = request.horaInicio() != null ? request.horaInicio() : blocoExistente.getHoraInicio();
        LocalTime horaFimEfetiva = request.horaFim() != null ? request.horaFim() : blocoExistente.getHoraFim();

        validarIntervaloHorario(horaInicioEfetiva, horaFimEfetiva);

        if (blocoHorarioTemplateRepository.existeSobreposicaoHorarioDesconsiderandoBloco(
                template, diaSemanaEfetivo, blocoExistente.getId(), horaInicioEfetiva, horaFimEfetiva)) {
            throw new BadRequestException("Já existe um bloco de horário conflitante para "
                    + diaSemanaEfetivo + " no intervalo informado.");
        }
    }

    public void validarBlocoPertenceTemplate(BlocoHorarioTemplate bloco, TemplateSemanal template) {
        if (!bloco.getTemplateSemanal().getId().equals(template.getId())) {
            throw new ForbiddenException("Bloco de horário não pertence ao template semanal informado.");
        }
    }

    public void validarMateriaETopico(Usuario usuario, Materia materia, Topico topico) {
        if (materia != null && !materia.getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("A matéria indicada não pertence ao usuário.");
        }
        if (topico != null && !topico.getMateria().getPlanoEstudo().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("O tópico indicado não pertence ao usuário.");
        }
        if (materia != null && topico != null && !topico.getMateria().getId().equals(materia.getId())) {
            throw new BadRequestException("O tópico indicado não pertence à matéria informada.");
        }
    }

    private void validarIntervaloHorario(LocalTime inicio, LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new BadRequestException("O horário de término deve ser posterior ao horário de início.");
        }
    }
}
