package com.kyofoundation.skillnapse.modules.edital.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.edital.entity.RascunhoEdital;
import com.kyofoundation.skillnapse.modules.edital.repository.RascunhoEditalRepository;
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
class RascunhoEditalFinderTest {

    @Mock
    private RascunhoEditalRepository rascunhoEditalRepository;

    private RascunhoEditalFinder finder;

    @BeforeEach
    void setUp() {
        finder = new RascunhoEditalFinder(rascunhoEditalRepository);
    }

    @Test
    @DisplayName("Deve retornar RascunhoEdital quando id existir")
    void deveRetornarRascunhoQuandoExistir() {
        UUID id = UUID.randomUUID();
        RascunhoEdital rascunho = RascunhoEdital.builder().id(id).nomeArquivo("edital.pdf").build();

        when(rascunhoEditalRepository.findById(id)).thenReturn(Optional.of(rascunho));

        RascunhoEdital resultado = finder.findById(id);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando id nao existir")
    void deveLancarResourceNotFoundExceptionQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(rascunhoEditalRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }
}
