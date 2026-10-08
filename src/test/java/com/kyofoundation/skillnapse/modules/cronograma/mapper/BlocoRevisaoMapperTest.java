package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoAcaoExcecao;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoOrigemBloco;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Flashcard;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlocoRevisaoMapperTest {

    @Test
    @DisplayName("[toBlocoTemplate] Deve mapear entidade BlocoHorarioTemplate com tipo REVISAO e tópicos")
    void deveMapearBlocoTemplateComSucesso() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Direito Constitucional").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Direitos Fundamentais").materia(materia).build();

        CriarBlocoRevisaoTemplateRequest request = new CriarBlocoRevisaoTemplateRequest(
                template.getId(),
                DiaSemana.QUARTA,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                materia.getId(),
                List.of(topico.getId())
        );

        BlocoHorarioTemplate bloco = BlocoRevisaoMapper.toBlocoTemplate(template, request, materia, Set.of(topico));

        assertThat(bloco.getTemplateSemanal()).isEqualTo(template);
        assertThat(bloco.getDiaSemana()).isEqualTo(DiaSemana.QUARTA);
        assertThat(bloco.getHoraInicio()).isEqualTo(LocalTime.of(14, 0));
        assertThat(bloco.getHoraFim()).isEqualTo(LocalTime.of(15, 30));
        assertThat(bloco.getTipoBloco()).isEqualTo(TipoBloco.REVISAO);
        assertThat(bloco.getMateria()).isEqualTo(materia);
        assertThat(bloco.getTopicosRevisao()).containsExactly(topico);
    }

    @Test
    @DisplayName("[toExcecaoDiaria] Deve mapear entidade ExcecaoDiaria com tipo REVISAO e BLOCO_AVULSO")
    void deveMapearExcecaoDiariaComSucesso() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Português").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Sintaxe").materia(materia).build();
        LocalDate data = LocalDate.of(2026, 10, 20);

        CriarBlocoRevisaoDiarioRequest request = new CriarBlocoRevisaoDiarioRequest(
                data,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                materia.getId(),
                List.of(topico.getId())
        );

        ExcecaoDiaria excecao = BlocoRevisaoMapper.toExcecaoDiaria(usuario, request, materia, Set.of(topico));

        assertThat(excecao.getUsuario()).isEqualTo(usuario);
        assertThat(excecao.getDataExcecao()).isEqualTo(data);
        assertThat(excecao.getTipoAcao()).isEqualTo(TipoAcaoExcecao.BLOCO_AVULSO);
        assertThat(excecao.getTipoBloco()).isEqualTo(TipoBloco.REVISAO);
        assertThat(excecao.getTopicosRevisao()).containsExactly(topico);
    }

    @Test
    @DisplayName("[toResponse] Deve mapear BlocoHorarioTemplate para BlocoRevisaoResponse")
    void deveMapearBlocoHorarioParaResponse() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Contabilidade").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Balancete").materia(materia).build();

        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEXTA)
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 30))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(Set.of(topico))
                .build();

        BlocoRevisaoResponse response = BlocoRevisaoMapper.toResponse(bloco);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(bloco.getId());
        assertThat(response.origem()).isEqualTo(TipoOrigemBloco.TEMPLATE);
        assertThat(response.templateSemanalId()).isEqualTo(template.getId());
        assertThat(response.diaSemana()).isEqualTo(DiaSemana.SEXTA);
        assertThat(response.tipoBloco()).isEqualTo(TipoBloco.REVISAO);
        assertThat(response.topicosRevisao()).hasSize(1);
        assertThat(response.topicosRevisao().get(0).titulo()).isEqualTo("Balancete");
    }

    @Test
    @DisplayName("[toResponse] Deve mapear ExcecaoDiaria para BlocoRevisaoResponse")
    void deveMapearExcecaoParaResponse() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("RLM").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Tabela Verdade").materia(materia).build();

        ExcecaoDiaria excecao = ExcecaoDiaria.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataExcecao(LocalDate.of(2026, 11, 5))
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(9, 30))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(Set.of(topico))
                .build();

        BlocoRevisaoResponse response = BlocoRevisaoMapper.toResponse(excecao);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(excecao.getId());
        assertThat(response.origem()).isEqualTo(TipoOrigemBloco.EXCECAO);
        assertThat(response.dataExcecao()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(response.topicosRevisao()).hasSize(1);
    }

    @Test
    @DisplayName("[toConteudoResponse] Deve mapear conteúdo com cards a revisar")
    void deveMapearConteudoComCards() {
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("TI").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("SQL").materia(materia).build();

        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(20, 0))
                .horaFim(LocalTime.of(21, 0))
                .tipoBloco(TipoBloco.REVISAO)
                .materia(materia)
                .topicosRevisao(Set.of(topico))
                .build();

        Baralho baralho = Baralho.builder().id(UUID.randomUUID()).titulo("Baralho SQL").build();
        Flashcard card = Flashcard.builder()
                .id(UUID.randomUUID())
                .baralho(baralho)
                .topico(topico)
                .frente("O que é INNER JOIN?")
                .verso("Junção interna")
                .proximaRevisao(LocalDate.now())
                .build();

        ConteudoBlocoRevisaoResponse conteudo = BlocoRevisaoMapper.toConteudoResponse(
                bloco,
                LocalDate.now(),
                List.of(card),
                10L
        );

        assertThat(conteudo).isNotNull();
        assertThat(conteudo.blocoId()).isEqualTo(bloco.getId());
        assertThat(conteudo.origem()).isEqualTo(TipoOrigemBloco.TEMPLATE);
        assertThat(conteudo.totalTopicos()).isEqualTo(1);
        assertThat(conteudo.totalCardsCadastrados()).isEqualTo(10L);
        assertThat(conteudo.totalCardsParaRevisar()).isEqualTo(1);
        assertThat(conteudo.cardsParaRevisar().get(0).frente()).isEqualTo("O que é INNER JOIN?");
        assertThat(conteudo.cardsParaRevisar().get(0).topicoTitulo()).isEqualTo("SQL");
    }

    @Test
    @DisplayName("[toResponse null] Deve retornar null quando bloco ou exceção for nula")
    void deveRetornarNullSeEntidadeNula() {
        assertThat(BlocoRevisaoMapper.toResponse((BlocoHorarioTemplate) null)).isNull();
        assertThat(BlocoRevisaoMapper.toResponse((ExcecaoDiaria) null)).isNull();
        assertThat(BlocoRevisaoMapper.toTopicoItemResponse(null)).isNull();
        assertThat(BlocoRevisaoMapper.toFlashcardItemResponse(null)).isNull();
    }
}
