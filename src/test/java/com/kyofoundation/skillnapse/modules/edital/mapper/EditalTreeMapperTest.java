package com.kyofoundation.skillnapse.modules.edital.mapper;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.edital.dto.response.RascunhoEditalResponse;
import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalArvoreEstruturada;
import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalMaterialItem;
import com.kyofoundation.skillnapse.modules.edital.dto.structure.EditalTopicoItem;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.enums.StatusRascunhoEdital;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditalTreeMapperTest {

    @Test
    @DisplayName("Deve serializar e desserializar EditalArvoreEstruturada corretamente")
    void deveSerializarEDesserializarArvore() {
        EditalTopicoItem topico = new EditalTopicoItem("Interpretação de Texto", 4, List.of("Coesão", "Coerência"));
        EditalMaterialItem materia = new EditalMaterialItem("Português", List.of(topico));
        EditalArvoreEstruturada arvore = new EditalArvoreEstruturada("Concurso BB", "Escriturário", List.of(materia));

        String json = EditalTreeMapper.toJson(arvore);
        assertThat(json).isNotBlank().contains("Concurso BB").contains("Português");

        EditalArvoreEstruturada reconstruida = EditalTreeMapper.fromJson(json);
        assertThat(reconstruida).isNotNull();
        assertThat(reconstruida.nomeConcurso()).isEqualTo("Concurso BB");
        assertThat(reconstruida.cargo()).isEqualTo("Escriturário");
        assertThat(reconstruida.materias()).hasSize(1);
        assertThat(reconstruida.materias().get(0).nome()).isEqualTo("Português");
    }

    @Test
    @DisplayName("Deve falhar ao desserializar JSON malformado")
    void deveFalharComJsonInvalido() {
        assertThatThrownBy(() -> EditalTreeMapper.fromJson("{ json_invalido: true "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Falha ao converter o JSON");
    }

    @Test
    @DisplayName("Deve converter para entidade e para response")
    void deveConverterParaEntidadeEResponse() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        String json = "{\"nomeConcurso\":\"BB\",\"cargo\":\"Agente\",\"materias\":[]}";

        RascunhoEdital rascunho = EditalTreeMapper.toEntity("edital.pdf", json, usuario);
        assertThat(rascunho).isNotNull();
        assertThat(rascunho.getNomeArquivo()).isEqualTo("edital.pdf");
        assertThat(rascunho.getStatus()).isEqualTo(StatusRascunhoEdital.AGUARDANDO_APROVACAO);

        RascunhoEditalResponse response = EditalTreeMapper.toResponse(rascunho);
        assertThat(response).isNotNull();
        assertThat(response.conteudo().nomeConcurso()).isEqualTo("BB");
    }
}
