package com.kyofoundation.skillnapse.modules.cronograma.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.RegistrarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ExcecaoDiariaValidator {

    private final ExcecaoDiariaRepository excecaoDiariaRepository;

    public void validarCriacao(RegistrarExcecaoDiariaRequest request, LocalDate data, Usuario usuario, BlocoHorarioTemplate blocoOrigem) {
        if (request == null) {
            throw new BadRequestException("Os dados da exceção diária não podem ser nulos.");
        }
        if (request.tipoAcao() == null) {
            throw new BadRequestException("O tipo de ação da exceção é obrigatório.");
        }
        if (data == null) {
            throw new BadRequestException("A data da exceção é obrigatória.");
        }

        DiaSemana diaDaData = DiaSemana.fromDayOfWeek(data.getDayOfWeek());

        if (request.tipoAcao() == TipoAcaoExcecao.CANCELAR_BLOCO) {
            if (blocoOrigem == null) {
                throw new BadRequestException("O bloco do template de origem é obrigatório para a ação CANCELAR_BLOCO.");
            }
            validarDiaSemanaBlocoOrigem(blocoOrigem, diaDaData);
            validarDuplicidadeBlocoOrigem(usuario, data, blocoOrigem);
        } else if (request.tipoAcao() == TipoAcaoExcecao.SUBSTITUIR_HORARIO) {
            if (blocoOrigem == null) {
                throw new BadRequestException("O bloco do template de origem é obrigatório para a ação SUBSTITUIR_HORARIO.");
            }
            validarDiaSemanaBlocoOrigem(blocoOrigem, diaDaData);
            validarDuplicidadeBlocoOrigem(usuario, data, blocoOrigem);

            LocalTime inicio = request.horaInicio() != null ? request.horaInicio() : blocoOrigem.getHoraInicio();
            LocalTime fim = request.horaFim() != null ? request.horaFim() : blocoOrigem.getHoraFim();
            validarIntervaloHorario(inicio, fim);
        } else if (request.tipoAcao() == TipoAcaoExcecao.BLOCO_AVULSO) {
            if (request.horaInicio() == null || request.horaFim() == null) {
                throw new BadRequestException("Horário de início e término são obrigatórios para BLOCO_AVULSO.");
            }
            validarIntervaloHorario(request.horaInicio(), request.horaFim());
        }
    }

    public void validarEdicao(EditarExcecaoDiariaRequest request, ExcecaoDiaria excecaoExistente, BlocoHorarioTemplate blocoOrigemNovo) {
        if (request == null) {
            throw new BadRequestException("Os dados de edição não podem ser nulos.");
        }

        LocalTime inicio = request.horaInicio() != null ? request.horaInicio() : excecaoExistente.getHoraInicio();
        LocalTime fim = request.horaFim() != null ? request.horaFim() : excecaoExistente.getHoraFim();

        if (inicio != null && fim != null) {
            validarIntervaloHorario(inicio, fim);
        }

        if (blocoOrigemNovo != null) {
            DiaSemana diaDaData = DiaSemana.fromDayOfWeek(excecaoExistente.getDataExcecao().getDayOfWeek());
            validarDiaSemanaBlocoOrigem(blocoOrigemNovo, diaDaData);

            if (excecaoDiariaRepository.existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigemAndIdNot(
                    excecaoExistente.getUsuario(), excecaoExistente.getDataExcecao(), blocoOrigemNovo, excecaoExistente.getId())) {
                throw new ConflictException("Já existe outra exceção registrada para este bloco nesta data.");
            }
        }
    }

    public void validarPropriedade(Usuario usuario, ExcecaoDiaria excecao) {
        if (!excecao.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("A exceção diária não pertence ao usuário autenticado.");
        }
    }

    public void validarPropriedadeBlocoTemplate(Usuario usuario, BlocoHorarioTemplate bloco) {
        if (bloco != null && !bloco.getTemplateSemanal().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("O bloco de template indicado não pertence ao usuário autenticado.");
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

    private void validarDiaSemanaBlocoOrigem(BlocoHorarioTemplate blocoOrigem, DiaSemana diaDaData) {
        if (!blocoOrigem.getDiaSemana().equals(diaDaData)) {
            throw new BadRequestException("O bloco de origem (" + blocoOrigem.getDiaSemana()
                    + ") não pertence ao dia da semana desta data (" + diaDaData + ").");
        }
    }

    private void validarDuplicidadeBlocoOrigem(Usuario usuario, LocalDate data, BlocoHorarioTemplate blocoOrigem) {
        if (excecaoDiariaRepository.existsByUsuarioAndDataExcecaoAndBlocoTemplateOrigem(usuario, data, blocoOrigem)) {
            throw new ConflictException("Já existe uma exceção registrada para este bloco nesta data.");
        }
    }

    private void validarIntervaloHorario(LocalTime inicio, LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new BadRequestException("O horário de término deve ser posterior ao horário de início.");
        }
    }
}
