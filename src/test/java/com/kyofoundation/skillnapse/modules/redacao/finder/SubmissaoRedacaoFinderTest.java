package com.kyofoundation.skillnapse.modules.redacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.redacao.entity.SubmissaoRedacao;
import com.kyofoundation.skillnapse.modules.redacao.repository.SubmissaoRedacaoRepository;
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
class SubmissaoRedacaoFinderTest {

    @Mock
    private SubmissaoRedacaoRepository submissaoRedacaoRepository;

    @InjectMocks
    private SubmissaoRedacaoFinder submissaoRedacaoFinder;

    @Test
    @DisplayName("Deve retornar SubmissaoRedacao quando ID existir")
    void deveRetornarSubmissaoQuandoExistir() {
        UUID id = UUID.randomUUID();
        SubmissaoRedacao submissao = SubmissaoRedacao.builder().id(id).build();

        when(submissaoRedacaoRepository.findById(id)).thenReturn(Optional.of(submissao));

        SubmissaoRedacao resultado = submissaoRedacaoFinder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando ID não existir")
    void deveLancarExcecaoQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(submissaoRedacaoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissaoRedacaoFinder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Submissão de redação não encontrada com o id: " + id);
    }
}
