package com.kyofoundation.skillnapse.modules.gamificacao.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.MetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.PainelGamificacaoResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.ProgressoMetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GamificacaoMapperTest {

    @Test
    @DisplayName("[toStatusResponse] Deve converter OfensivaUsuario para StatusOfensivaResponse")
    void deveConverterStatusOfensiva() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        OfensivaUsuario ofensiva = OfensivaUsuario.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .diasConsecutivosAtual(5)
                .maiorSequenciaDias(12)
                .dataUltimoEstudo(LocalDate.of(2026, 10, 8))
                .build();

        StatusOfensivaResponse response = GamificacaoMapper.toStatusResponse(ofensiva, true, true);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ofensiva.getId());
        assertThat(response.usuarioId()).isEqualTo(usuario.getId());
        assertThat(response.diasConsecutivosAtual()).isEqualTo(5);
        assertThat(response.maiorSequenciaDias()).isEqualTo(12);
        assertThat(response.dataUltimoEstudo()).isEqualTo(LocalDate.of(2026, 10, 8));
        assertThat(response.estudouHoje()).isTrue();
        assertThat(response.ofensivaAtiva()).isTrue();
    }

    @Test
    @DisplayName("[toMetaResponse] Deve converter MetaDiaria para MetaDiariaResponse")
    void deveConverterMetaDiaria() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        MetaDiaria meta = MetaDiaria.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .metaMinutosEstudo(150)
                .metaQuestoesResolvidas(25)
                .build();

        MetaDiariaResponse response = GamificacaoMapper.toMetaResponse(meta);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(meta.getId());
        assertThat(response.usuarioId()).isEqualTo(usuario.getId());
        assertThat(response.metaMinutosEstudo()).isEqualTo(150);
        assertThat(response.metaQuestoesResolvidas()).isEqualTo(25);
    }

    @Test
    @DisplayName("[aplicarAtualizacaoMeta] Deve atualizar campos da entidade")
    void deveAplicarAtualizacaoMeta() {
        MetaDiaria meta = MetaDiaria.builder().metaMinutosEstudo(120).metaQuestoesResolvidas(15).build();
        AtualizarMetaDiariaRequest request = new AtualizarMetaDiariaRequest(180, 30);

        GamificacaoMapper.aplicarAtualizacaoMeta(meta, request);

        assertThat(meta.getMetaMinutosEstudo()).isEqualTo(180);
        assertThat(meta.getMetaQuestoesResolvidas()).isEqualTo(30);
    }

    @Test
    @DisplayName("[toPainelResponse] Deve compor PainelGamificacaoResponse")
    void deveComporPainelGamificacao() {
        StatusOfensivaResponse ofensiva = new StatusOfensivaResponse(
                UUID.randomUUID(), UUID.randomUUID(), 3, 5, LocalDate.now(), true, true
        );
        ProgressoMetaDiariaResponse progresso = new ProgressoMetaDiariaResponse(
                LocalDate.now(), 120, 60, 50.0, false, 15, 10, 66.7, false, false
        );

        PainelGamificacaoResponse painel = GamificacaoMapper.toPainelResponse(ofensiva, progresso);

        assertThat(painel).isNotNull();
        assertThat(painel.ofensiva()).isEqualTo(ofensiva);
        assertThat(painel.progressoHoje()).isEqualTo(progresso);
    }

    @Test
    @DisplayName("[null safety] Deve retornar null quando entidade nula")
    void deveRetornarNullSeEntidadeNula() {
        assertThat(GamificacaoMapper.toStatusResponse(null, false, false)).isNull();
        assertThat(GamificacaoMapper.toMetaResponse(null)).isNull();
    }
}
