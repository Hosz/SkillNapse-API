package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
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
class BlocoHorarioTemplateFinderTest {

    @Mock
    private BlocoHorarioTemplateRepository repository;

    @InjectMocks
    private BlocoHorarioTemplateFinder finder;

    @Test
    @DisplayName("Deve encontrar bloco de horario por ID com sucesso")
    void deveEncontrarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(bloco));

        BlocoHorarioTemplate resultado = finder.findById(id);

        assertThat(resultado).isEqualTo(bloco);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando bloco nao for encontrado")
    void deveLancarExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bloco de horário não encontrado ou não existente.");
    }
}
