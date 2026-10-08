package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.FlashcardRevisaoItemResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TopicoRevisaoItemResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class BlocoRevisaoMapper {

    public static BlocoHorarioTemplate toBlocoTemplate(
            TemplateSemanal template,
            CriarBlocoRevisaoTemplateRequest request,
            Materia materia,
            Set<Topico> topicos) {

        return BlocoHorarioTemplate.builder()
                .templateSemanal(template)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(topicos != null ? new HashSet<>(topicos) : new HashSet<>())
                .build();
    }

    public static ExcecaoDiaria toExcecaoDiaria(
            Usuario usuario,
            CriarBlocoRevisaoDiarioRequest request,
            Materia materia,
            Set<Topico> topicos) {

        return ExcecaoDiaria.builder()
                .usuario(usuario)
                .dataExcecao(request.dataExcecao())
                .tipoAcao(TipoAcaoExcecao.BLOCO_AVULSO)
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(topicos != null ? new HashSet<>(topicos) : new HashSet<>())
                .build();
    }

    public static BlocoRevisaoResponse toResponse(BlocoHorarioTemplate bloco) {
        if (bloco == null) {
            return null;
        }

        UUID templateId = bloco.getTemplateSemanal() != null ? bloco.getTemplateSemanal().getId() : null;
        UUID materiaId = bloco.getMateria() != null ? bloco.getMateria().getId() : null;
        String materiaNome = bloco.getMateria() != null ? bloco.getMateria().getNome() : null;

        List<TopicoRevisaoItemResponse> topicos = mapTopicos(bloco.getTopicosRevisao());

        return new BlocoRevisaoResponse(
                bloco.getId(),
                TipoOrigemBloco.TEMPLATE,
                templateId,
                bloco.getDiaSemana(),
                null,
                bloco.getHoraInicio(),
                bloco.getHoraFim(),
                bloco.getTipoBloco(),
                materiaId,
                materiaNome,
                topicos
        );
    }

    public static BlocoRevisaoResponse toResponse(ExcecaoDiaria excecao) {
        if (excecao == null) {
            return null;
        }

        UUID materiaId = excecao.getMateria() != null ? excecao.getMateria().getId() : null;
        String materiaNome = excecao.getMateria() != null ? excecao.getMateria().getNome() : null;

        List<TopicoRevisaoItemResponse> topicos = mapTopicos(excecao.getTopicosRevisao());

        return new BlocoRevisaoResponse(
                excecao.getId(),
                TipoOrigemBloco.EXCECAO,
                null,
                null,
                excecao.getDataExcecao(),
                excecao.getHoraInicio(),
                excecao.getHoraFim(),
                excecao.getTipoBloco(),
                materiaId,
                materiaNome,
                topicos
        );
    }

    public static ConteudoBlocoRevisaoResponse toConteudoResponse(
            BlocoHorarioTemplate bloco,
            LocalDate dataReferencia,
            List<Flashcard> cardsVencidos,
            long totalCardsCadastrados) {

        UUID materiaId = bloco.getMateria() != null ? bloco.getMateria().getId() : null;
        String materiaNome = bloco.getMateria() != null ? bloco.getMateria().getNome() : null;
        List<TopicoRevisaoItemResponse> topicos = mapTopicos(bloco.getTopicosRevisao());
        List<FlashcardRevisaoItemResponse> cards = mapFlashcards(cardsVencidos);

        return new ConteudoBlocoRevisaoResponse(
                bloco.getId(),
                TipoOrigemBloco.TEMPLATE,
                dataReferencia,
                bloco.getHoraInicio(),
                bloco.getHoraFim(),
                materiaId,
                materiaNome,
                topicos.size(),
                topicos,
                totalCardsCadastrados,
                cards.size(),
                cards
        );
    }

    public static ConteudoBlocoRevisaoResponse toConteudoResponse(
            ExcecaoDiaria excecao,
            LocalDate dataReferencia,
            List<Flashcard> cardsVencidos,
            long totalCardsCadastrados) {

        UUID materiaId = excecao.getMateria() != null ? excecao.getMateria().getId() : null;
        String materiaNome = excecao.getMateria() != null ? excecao.getMateria().getNome() : null;
        List<TopicoRevisaoItemResponse> topicos = mapTopicos(excecao.getTopicosRevisao());
        List<FlashcardRevisaoItemResponse> cards = mapFlashcards(cardsVencidos);

        return new ConteudoBlocoRevisaoResponse(
                excecao.getId(),
                TipoOrigemBloco.EXCECAO,
                dataReferencia,
                excecao.getHoraInicio(),
                excecao.getHoraFim(),
                materiaId,
                materiaNome,
                topicos.size(),
                topicos,
                totalCardsCadastrados,
                cards.size(),
                cards
        );
    }

    public static TopicoRevisaoItemResponse toTopicoItemResponse(Topico topico) {
        if (topico == null) {
            return null;
        }
        UUID materiaId = topico.getMateria() != null ? topico.getMateria().getId() : null;
        String materiaNome = topico.getMateria() != null ? topico.getMateria().getNome() : null;
        return new TopicoRevisaoItemResponse(topico.getId(), topico.getTitulo(), materiaId, materiaNome);
    }

    public static FlashcardRevisaoItemResponse toFlashcardItemResponse(Flashcard card) {
        if (card == null) {
            return null;
        }
        UUID baralhoId = card.getBaralho() != null ? card.getBaralho().getId() : null;
        String baralhoTitulo = card.getBaralho() != null ? card.getBaralho().getTitulo() : null;
        UUID topicoId = card.getTopico() != null ? card.getTopico().getId() : null;
        String topicoTitulo = card.getTopico() != null ? card.getTopico().getTitulo() : null;

        return new FlashcardRevisaoItemResponse(
                card.getId(),
                baralhoId,
                baralhoTitulo,
                topicoId,
                topicoTitulo,
                card.getFrente(),
                card.getVerso(),
                card.getFatorFacilidade(),
                card.getIntervaloDias() != null ? card.getIntervaloDias() : 0,
                card.getRepeticoes() != null ? card.getRepeticoes() : 0,
                card.getProximaRevisao()
        );
    }

    private static List<TopicoRevisaoItemResponse> mapTopicos(Set<Topico> topicos) {
        if (topicos == null || topicos.isEmpty()) {
            return List.of();
        }
        return topicos.stream()
                .map(BlocoRevisaoMapper::toTopicoItemResponse)
                .sorted(Comparator.comparing(TopicoRevisaoItemResponse::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static List<FlashcardRevisaoItemResponse> mapFlashcards(List<Flashcard> cards) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        return cards.stream()
                .map(BlocoRevisaoMapper::toFlashcardItemResponse)
                .toList();
    }
}
