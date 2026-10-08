package com.kyofoundation.skillnapse.modules.questao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarSimuladoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.enums.TipoSimulado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimuladoMapperTest {

    @Test
    @DisplayName("Deve converter CriarSimuladoRequest para Simulado entity com tipo padrão MANUAL")
    void deveConverterParaEntityComTipoDefault() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        CriarSimuladoRequest request = new CriarSimuladoRequest("Simulado Inicial", null);

        Simulado simulado = SimuladoMapper.toEntity(request, usuario);

        assertThat(simulado).isNotNull();
        assertThat(simulado.getTitulo()).isEqualTo("Simulado Inicial");
        assertThat(simulado.getTipo()).isEqualTo(TipoSimulado.MANUAL);
        assertThat(simulado.getConcluido()).isFalse();
        assertThat(simulado.getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Deve converter Simulado para SimuladoResponse e calcular percentual com 2 casas")
    void deveConverterParaResponseComPercentual() {
        Simulado simulado = Simulado.builder()
                .id(UUID.randomUUID())
                .titulo("Simulado TRF")
                .tipo(TipoSimulado.MANUAL)
                .concluido(true)
                .criadoEm(Instant.now())
                .build();

        SimuladoResponse response = SimuladoMapper.toResponse(simulado, 10L, 7L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(simulado.getId());
        assertThat(response.totalQuestoesRespondidas()).isEqualTo(10L);
        assertThat(response.totalAcertos()).isEqualTo(7L);
        assertThat(response.percentualAcerto()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("Deve retornar percentual 0 quando não houver questões respondidas")
    void deveRetornarPercentualZeroSemQuestoes() {
        Simulado simulado = Simulado.builder()
                .id(UUID.randomUUID())
                .titulo("Simulado Vazio")
                .tipo(TipoSimulado.MANUAL)
                .concluido(false)
                .criadoEm(Instant.now())
                .build();

        SimuladoResponse response = SimuladoMapper.toResponse(simulado, 0L, 0L);

        assertThat(response).isNotNull();
        assertThat(response.percentualAcerto()).isEqualTo(0.0);
    }
}
