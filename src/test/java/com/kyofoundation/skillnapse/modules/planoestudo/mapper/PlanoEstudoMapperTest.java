package com.kyofoundation.skillnapse.modules.planoestudo.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanoEstudoMapperTest {

    @Test
    @DisplayName("Deve converter request para criacao de PlanoEstudo")
    void deveConverterCriarPlano() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).nome("Aluno").build();
        PlanoEstudoRequest request = new PlanoEstudoRequest("Plano Magistratura", "Foco prova 2026");

        PlanoEstudo plano = PlanoEstudoMapper.criarPlano(request, usuario);

        assertThat(plano).isNotNull();
        assertThat(plano.getTitulo()).isEqualTo("Plano Magistratura");
        assertThat(plano.getDescricao()).isEqualTo("Foco prova 2026");
        assertThat(plano.getUsuario()).isEqualTo(usuario);
        assertThat(plano.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve converter PlanoEstudo para response DTO")
    void deveConverterParaResponse() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        PlanoEstudo plano = PlanoEstudo.builder()
                .id(id)
                .titulo("Plano Delegado")
                .descricao("Treino físico e jurídico")
                .ativo(true)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();

        PlanoEstudoResponse response = PlanoEstudoMapper.toResponse(plano);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.titulo()).isEqualTo("Plano Delegado");
        assertThat(response.descricao()).isEqualTo("Treino físico e jurídico");
        assertThat(response.ativo()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar PlanoEstudo existente com campos de edicao")
    void deveAtualizarPlanoComEdicao() {
        PlanoEstudo plano = PlanoEstudo.builder()
                .titulo("Titulo Velho")
                .descricao("Descricao Velha")
                .ativo(true)
                .build();

        EdicaoPlanoEstudoRequest request = new EdicaoPlanoEstudoRequest("Titulo Novo", "Descricao Nova", false);

        PlanoEstudoMapper.toEditarPlano(plano, request);

        assertThat(plano.getTitulo()).isEqualTo("Titulo Novo");
        assertThat(plano.getDescricao()).isEqualTo("Descricao Nova");
        assertThat(plano.getAtivo()).isFalse();
    }
}
