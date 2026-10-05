package com.kyofoundation.skillnapse.modules.planoestudo.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository;
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
class MateriaFinderTest {

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private MateriaFinder materiaFinder;

    @Test
    @DisplayName("Deve encontrar materia por ID com sucesso")
    void deveEncontrarMateriaPorIdComSucesso() {
        UUID materiaId = UUID.randomUUID();
        Materia materia = Materia.builder().id(materiaId).nome("Direito Civil").build();

        when(materiaRepository.findById(materiaId)).thenReturn(Optional.of(materia));

        Materia resultado = materiaFinder.findById(materiaId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(materiaId);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando materia nao for encontrada")
    void deveLancarExcecaoQuandoMateriaNaoEncontrada() {
        UUID materiaId = UUID.randomUUID();
        when(materiaRepository.findById(materiaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaFinder.findById(materiaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matéria não encontrada ou não existe.");
    }
}
