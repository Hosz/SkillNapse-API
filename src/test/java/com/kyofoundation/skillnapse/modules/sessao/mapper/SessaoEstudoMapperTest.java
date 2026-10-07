package com.kyofoundation.skillnapse.modules.sessao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.sessao.dto.request.RegistrarSessaoEstudoRequest;
import com.kyofoundation.skillnapse.modules.sessao.dto.response.SessaoEstudoResponse;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.enums.StatusSessaoEstudo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessaoEstudoMapperTest {

    @Test
    @DisplayName("Deve converter RegistrarSessaoEstudoRequest para SessaoEstudo corretamente")
    void deveConverterRequestParaEntidade() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).build();
        Instant agora = Instant.now();

        RegistrarSessaoEstudoRequest request = new RegistrarSessaoEstudoRequest(
                topico.getId(),
                agora.minusSeconds(1800),
                agora,
                1500,
                StatusSessaoEstudo.CONCLUIDA,
                "Observação de teste"
        );

        SessaoEstudo entity = SessaoEstudoMapper.toEntity(request, usuario, topico);

        assertThat(entity).isNotNull();
        assertThat(entity.getUsuario()).isEqualTo(usuario);
        assertThat(entity.getTopico()).isEqualTo(topico);
        assertThat(entity.getIniciadoEm()).isEqualTo(request.iniciadoEm());
        assertThat(entity.getFinalizadoEm()).isEqualTo(request.finalizadoEm());
        assertThat(entity.getDuracaoLiquidaSegundos()).isEqualTo(1500);
        assertThat(entity.getStatus()).isEqualTo(StatusSessaoEstudo.CONCLUIDA);
        assertThat(entity.getObservacoes()).isEqualTo("Observação de teste");
    }

    @Test
    @DisplayName("Deve converter SessaoEstudo para SessaoEstudoResponse com navegacao segura")
    void deveConverterEntidadeParaResponseComNavegacaoSegura() {
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Direito Constitucional").build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).titulo("Direitos Fundamentais").materia(materia).build();
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        Instant inicio = Instant.now().minusSeconds(3600);
        Instant fim = Instant.now();

        SessaoEstudo entity = SessaoEstudo.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .topico(topico)
                .iniciadoEm(inicio)
                .finalizadoEm(fim)
                .duracaoLiquidaSegundos(3200)
                .status(StatusSessaoEstudo.CONCLUIDA)
                .observacoes("Notas")
                .build();

        SessaoEstudoResponse response = SessaoEstudoMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.topicoId()).isEqualTo(topico.getId());
        assertThat(response.topicoTitulo()).isEqualTo("Direitos Fundamentais");
        assertThat(response.materiaId()).isEqualTo(materia.getId());
        assertThat(response.materiaNome()).isEqualTo("Direito Constitucional");
        assertThat(response.duracaoLiquidaSegundos()).isEqualTo(3200);
    }

    @Test
    @DisplayName("Deve tratar topico ou materia nulos sem disparar NullPointerException")
    void deveTratarRelacionamentosNulosSemNpe() {
        SessaoEstudo entity = SessaoEstudo.builder()
                .id(UUID.randomUUID())
                .topico(null)
                .duracaoLiquidaSegundos(100)
                .build();

        SessaoEstudoResponse response = SessaoEstudoMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.topicoId()).isNull();
        assertThat(response.topicoTitulo()).isNull();
        assertThat(response.materiaId()).isNull();
        assertThat(response.materiaNome()).isNull();
    }
}
