package com.kyofoundation.skillnapse.modules.planoestudo.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.PlanoEstudoRepository;
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
class PlanoEstudoFinderTest {

    @Mock
    private PlanoEstudoRepository planoEstudoRepository;

    @InjectMocks
    private PlanoEstudoFinder planoEstudoFinder;

    @Test
    @DisplayName("Deve encontrar plano de estudo por ID com sucesso")
    void deveEncontrarPlanoPorIdComSucesso() {
        UUID planoId = UUID.randomUUID();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).titulo("Concurso TCU").build();

        when(planoEstudoRepository.findById(planoId)).thenReturn(Optional.of(plano));

        PlanoEstudo resultado = planoEstudoFinder.findById(planoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(planoId);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando plano de estudo nao for encontrado")
    void deveLancarExcecaoQuandoPlanoNaoEncontrado() {
        UUID planoId = UUID.randomUUID();
        when(planoEstudoRepository.findById(planoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planoEstudoFinder.findById(planoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("O plano de estudo indicado não foi encontrado.");
    }
}
