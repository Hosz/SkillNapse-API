package com.kyofoundation.skillnapse.modules.redacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.redacao.entity.TemaRedacao;
import com.kyofoundation.skillnapse.modules.redacao.repository.TemaRedacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemaRedacaoFinderTest {

    @Mock
    private TemaRedacaoRepository temaRedacaoRepository;

    @InjectMocks
    private TemaRedacaoFinder temaRedacaoFinder;

    @Test
    @DisplayName("Deve retornar TemaRedacao quando ID existir")
    void deveRetornarTemaQuandoExistir() {
        UUID id = UUID.randomUUID();
        TemaRedacao tema = TemaRedacao.builder().id(id).titulo("Tema Teste").build();

        when(temaRedacaoRepository.findById(id)).thenReturn(Optional.of(tema));

        TemaRedacao resultado = temaRedacaoFinder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
        assertThat(resultado.getTitulo()).isEqualTo("Tema Teste");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando ID não existir")
    void deveLancarExcecaoQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(temaRedacaoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> temaRedacaoFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tema de redação não encontrado com o id: " + id);
    }
}
