package com.kyofoundation.skillnapse.modules.planoestudo.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
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
class TopicoFinderTest {

    @Mock
    private TopicoRepository topicoRepository;

    @InjectMocks
    private TopicoFinder topicoFinder;

    @Test
    @DisplayName("Deve encontrar topico por ID com sucesso")
    void deveEncontrarTopicoPorIdComSucesso() {
        UUID topicoId = UUID.randomUUID();
        Topico topico = Topico.builder().id(topicoId).titulo("Contratos Administrativos").build();

        when(topicoRepository.findById(topicoId)).thenReturn(Optional.of(topico));

        Topico resultado = topicoFinder.findById(topicoId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(topicoId);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando topico nao for encontrado")
    void deveLancarExcecaoQuandoTopicoNaoEncontrado() {
        UUID topicoId = UUID.randomUUID();
        when(topicoRepository.findById(topicoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicoFinder.findById(topicoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tópico não encontrado ou não existente.");
    }
}
