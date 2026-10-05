package com.kyofoundation.skillnapse.modules.planoestudo.mapper;

import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.MateriaResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MateriaMapperTest {

    @Test
    @DisplayName("Deve converter request para criacao de Materia com ordem default 0")
    void deveConverterCriarMateria() {
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).build();
        CriarMateriaRequest request = new CriarMateriaRequest("Direito Empresarial", "#10B981", null);

        Materia materia = MateriaMapper.toCriacaoMateria(request, plano);

        assertThat(materia).isNotNull();
        assertThat(materia.getNome()).isEqualTo("Direito Empresarial");
        assertThat(materia.getCorHex()).isEqualTo("#10B981");
        assertThat(materia.getOrdem()).isEqualTo(0);
        assertThat(materia.getPlanoEstudo()).isEqualTo(plano);
    }

    @Test
    @DisplayName("Deve converter Materia para response DTO")
    void deveConverterParaResponse() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        Materia materia = Materia.builder()
                .id(id)
                .nome("Direito Ambiental")
                .corHex("#059669")
                .ordem(2)
                .criadoEm(agora)
                .build();

        MateriaResponse response = MateriaMapper.toResponse(materia);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.nome()).isEqualTo("Direito Ambiental");
        assertThat(response.corHex()).isEqualTo("#059669");
        assertThat(response.ordem()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve atualizar Materia com campos de edicao")
    void deveAtualizarMateriaComEdicao() {
        Materia materia = Materia.builder()
                .nome("Nome Velho")
                .corHex("#000000")
                .ordem(1)
                .build();

        EditarMateriaRequest request = new EditarMateriaRequest("Nome Novo", "#FFFFFF", 5);

        MateriaMapper.toEditarMateria(materia, request);

        assertThat(materia.getNome()).isEqualTo("Nome Novo");
        assertThat(materia.getCorHex()).isEqualTo("#FFFFFF");
        assertThat(materia.getOrdem()).isEqualTo(5);
    }
}
