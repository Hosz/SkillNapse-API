package com.kyofoundation.skillnapse.modules.sessao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.repository.SessaoEstudoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoEstudoFinderTest {

    @Mock
    private SessaoEstudoRepository sessaoEstudoRepository;

    private SessaoEstudoFinder finder;

    @BeforeEach
    void setUp() {
        finder = new SessaoEstudoFinder(sessaoEstudoRepository);
    }

    @Test
    @DisplayName("Deve retornar SessaoEstudo quando id existir")
    void deveRetornarSessaoQuandoExistir() {
        UUID id = UUID.randomUUID();
        SessaoEstudo sessao = SessaoEstudo.builder().id(id).build();

        when(sessaoEstudoRepository.findById(id)).thenReturn(Optional.of(sessao));

        SessaoEstudo resultado = finder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando id nao existir")
    void deveLancarResourceNotFoundExceptionQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(sessaoEstudoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("não encontrada");
    }
}
