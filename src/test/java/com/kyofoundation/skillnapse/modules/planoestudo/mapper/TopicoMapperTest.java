package com.kyofoundation.skillnapse.modules.planoestudo.mapper;

import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.AtualizarProgressoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.TopicoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TopicoMapperTest {

    @Test
    @DisplayName("Deve converter request para criacao de Topico raiz com defaults")
    void deveConverterCriarTopicoRaiz() {
        Materia materia = Materia.builder().id(UUID.randomUUID()).build();
        CriarTopicoRequest request = new CriarTopicoRequest("Poder Constituinte", null, null, null, null);

        Topico topico = TopicoMapper.toCriarTopico(materia, null, request);

        assertThat(topico).isNotNull();
        assertThat(topico.getTitulo()).isEqualTo("Poder Constituinte");
        assertThat(topico.getPesoEdital()).isEqualTo(1);
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.INICIANTE);
        assertThat(topico.getConcluido()).isFalse();
        assertThat(topico.getOrdem()).isEqualTo(0);
        assertThat(topico.getTopicoPai()).isNull();
    }

    @Test
    @DisplayName("Deve converter request para criacao de subtópico com topicoPai")
    void deveConverterCriarSubtopico() {
        Materia materia = Materia.builder().id(UUID.randomUUID()).build();
        Topico topicoPai = Topico.builder().id(UUID.randomUUID()).build();
        CriarTopicoRequest request = new CriarTopicoRequest("Origens e Mutação", topicoPai.getId(), 3, NivelProficiencia.INTERMEDIARIO, 2);

        Topico topico = TopicoMapper.toCriarTopico(materia, topicoPai, request);

        assertThat(topico).isNotNull();
        assertThat(topico.getTitulo()).isEqualTo("Origens e Mutação");
        assertThat(topico.getPesoEdital()).isEqualTo(3);
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.INTERMEDIARIO);
        assertThat(topico.getTopicoPai()).isEqualTo(topicoPai);
        assertThat(topico.getOrdem()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve converter Topico com arvore de subtopicos para response DTO")
    void deveConverterParaResponseComArvore() {
        UUID materiaId = UUID.randomUUID();
        Materia materia = Materia.builder().id(materiaId).build();

        Topico filho = Topico.builder()
                .id(UUID.randomUUID())
                .materia(materia)
                .titulo("Subtópico Filho")
                .nivelProficiencia(NivelProficiencia.INICIANTE)
                .pesoEdital(1)
                .concluido(false)
                .ordem(0)
                .build();

        Topico pai = Topico.builder()
                .id(UUID.randomUUID())
                .materia(materia)
                .titulo("Tópico Pai")
                .nivelProficiencia(NivelProficiencia.AVANCADO)
                .pesoEdital(4)
                .concluido(true)
                .ordem(1)
                .subtopicos(List.of(filho))
                .build();

        TopicoResponse response = TopicoMapper.toResponse(pai);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(pai.getId());
        assertThat(response.materiaId()).isEqualTo(materiaId);
        assertThat(response.titulo()).isEqualTo("Tópico Pai");
        assertThat(response.subtopicos()).hasSize(1);
        assertThat(response.subtopicos().getFirst().titulo()).isEqualTo("Subtópico Filho");
    }

    @Test
    @DisplayName("Deve editar campos de Topico com EditarTopicoRequest")
    void deveEditarTopico() {
        Topico topico = Topico.builder()
                .titulo("Titulo Velho")
                .pesoEdital(1)
                .nivelProficiencia(NivelProficiencia.INICIANTE)
                .concluido(false)
                .ordem(0)
                .build();

        EditarTopicoRequest request = new EditarTopicoRequest("Titulo Novo", 5, NivelProficiencia.AVANCADO, true, 3);

        TopicoMapper.toEditarTopico(topico, request);

        assertThat(topico.getTitulo()).isEqualTo("Titulo Novo");
        assertThat(topico.getPesoEdital()).isEqualTo(5);
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.AVANCADO);
        assertThat(topico.getConcluido()).isTrue();
        assertThat(topico.getOrdem()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve atualizar progresso e proficiencia com AtualizarProgressoRequest")
    void deveAtualizarProgresso() {
        Topico topico = Topico.builder()
                .concluido(false)
                .nivelProficiencia(NivelProficiencia.INICIANTE)
                .build();

        AtualizarProgressoRequest request = new AtualizarProgressoRequest(true, NivelProficiencia.INTERMEDIARIO);

        TopicoMapper.toAtualizarProgresso(topico, request);

        assertThat(topico.getConcluido()).isTrue();
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.INTERMEDIARIO);
    }
}
