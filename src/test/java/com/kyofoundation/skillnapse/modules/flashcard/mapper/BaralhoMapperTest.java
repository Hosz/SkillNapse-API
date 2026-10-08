package com.kyofoundation.skillnapse.modules.flashcard.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaralhoMapperTest {

    @Test
    @DisplayName("Deve converter CriarBaralhoRequest em entidade Baralho")
    void deveConverterRequestEmEntidade() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Direito").build();
        CriarBaralhoRequest request = new CriarBaralhoRequest("Constitucional", "Descrição teste", materia.getId());

        Baralho entity = BaralhoMapper.toEntity(request, usuario, materia);

        assertThat(entity).isNotNull();
        assertThat(entity.getUsuario()).isEqualTo(usuario);
        assertThat(entity.getMateria()).isEqualTo(materia);
        assertThat(entity.getTitulo()).isEqualTo("Constitucional");
        assertThat(entity.getDescricao()).isEqualTo("Descrição teste");
    }

    @Test
    @DisplayName("Deve atualizar entidade Baralho existente")
    void deveAtualizarEntidadeExistente() {
        Baralho entity = Baralho.builder()
                .titulo("Título Antigo")
                .descricao("Descrição Antiga")
                .build();

        Materia novaMateria = Materia.builder().id(UUID.randomUUID()).nome("Nova Matéria").build();
        AtualizarBaralhoRequest request = new AtualizarBaralhoRequest("Título Novo", "Descrição Nova", novaMateria.getId());

        BaralhoMapper.updateEntity(entity, request, novaMateria);

        assertThat(entity.getTitulo()).isEqualTo("Título Novo");
        assertThat(entity.getDescricao()).isEqualTo("Descrição Nova");
        assertThat(entity.getMateria()).isEqualTo(novaMateria);
    }

    @Test
    @DisplayName("Deve converter Baralho em BaralhoResponse com metricas")
    void deveConverterEntidadeEmResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Processo Civil").build();
        Instant agora = Instant.now();

        Baralho entity = Baralho.builder()
                .id(id)
                .usuario(usuario)
                .materia(materia)
                .titulo("CPC 2015")
                .descricao("Prazos recursais")
                .criadoEm(agora)
                .build();

        BaralhoResponse response = BaralhoMapper.toResponse(entity, 30L, 10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.usuarioId()).isEqualTo(userId);
        assertThat(response.materiaId()).isEqualTo(materia.getId());
        assertThat(response.materiaNome()).isEqualTo("Processo Civil");
        assertThat(response.titulo()).isEqualTo("CPC 2015");
        assertThat(response.totalCards()).isEqualTo(30L);
        assertThat(response.totalCardsParaRevisar()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve retornar null ao converter entidade nula")
    void deveRetornarNullParaEntidadeNula() {
        assertThat(BaralhoMapper.toResponse(null, 0L, 0L)).isNull();
    }
}
