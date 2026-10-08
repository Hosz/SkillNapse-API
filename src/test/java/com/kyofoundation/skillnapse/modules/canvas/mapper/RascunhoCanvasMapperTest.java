package com.kyofoundation.skillnapse.modules.canvas.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RascunhoCanvasMapperTest {

    @Test
    @DisplayName("Deve converter request em entidade RascunhoCanvas com sucesso")
    void deveConverterRequestEmEntidade() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Direito Constitucional").build();
        String json = "{\"paths\":[{\"stroke\":\"#ff0000\"}]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topico.getId(), json);

        RascunhoCanvas entity = RascunhoCanvasMapper.toEntity(request, usuario, topico);

        assertThat(entity).isNotNull();
        assertThat(entity.getUsuario()).isEqualTo(usuario);
        assertThat(entity.getTopico()).isEqualTo(topico);
        assertThat(entity.getDadosDesenhoJson()).isEqualTo(json);
        assertThat(entity.getAtualizadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar dadosDesenhoJson de entidade existente")
    void deveAtualizarEntidadeExistente() {
        RascunhoCanvas entity = RascunhoCanvas.builder()
                .id(UUID.randomUUID())
                .dadosDesenhoJson("{\"paths\":[]}")
                .atualizadoEm(Instant.EPOCH)
                .build();

        String novoJson = "{\"paths\":[{\"x\":1,\"y\":2}]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(null, novoJson);

        RascunhoCanvasMapper.updateEntity(entity, request);

        assertThat(entity.getDadosDesenhoJson()).isEqualTo(novoJson);
        assertThat(entity.getAtualizadoEm()).isAfter(Instant.EPOCH);
    }

    @Test
    @DisplayName("Deve converter entidade RascunhoCanvas em RascunhoCanvasResponse")
    void deveConverterEntidadeEmResponse() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        Instant agora = Instant.now();
        String json = "{\"elements\":[]}";

        Usuario usuario = Usuario.builder().id(usuarioId).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Princípios Fundamentais").build();

        RascunhoCanvas entity = RascunhoCanvas.builder()
                .id(id)
                .usuario(usuario)
                .topico(topico)
                .dadosDesenhoJson(json)
                .atualizadoEm(agora)
                .build();

        RascunhoCanvasResponse response = RascunhoCanvasMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.usuarioId()).isEqualTo(usuarioId);
        assertThat(response.topicoId()).isEqualTo(topicoId);
        assertThat(response.topicoTitulo()).isEqualTo("Princípios Fundamentais");
        assertThat(response.dadosDesenhoJson()).isEqualTo(json);
        assertThat(response.atualizadoEm()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Deve retornar null ao converter entidade nula")
    void deveRetornarNullParaEntidadeNula() {
        RascunhoCanvasResponse response = RascunhoCanvasMapper.toResponse(null);
        assertThat(response).isNull();
    }
}
