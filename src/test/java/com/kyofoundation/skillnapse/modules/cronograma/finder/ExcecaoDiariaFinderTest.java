package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.repository.ExcecaoDiariaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcecaoDiariaFinderTest {

    @Mock
    private ExcecaoDiariaRepository repository;

    @InjectMocks
    private ExcecaoDiariaFinder finder;

    @Test
    @DisplayName("Deve encontrar excecao diaria por ID com sucesso")
    void deveEncontrarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        ExcecaoDiaria excecao = ExcecaoDiaria.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(excecao));

        ExcecaoDiaria resultado = finder.findById(id);

        assertThat(resultado).isEqualTo(excecao);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando excecao nao for encontrada")
    void deveLancarExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exceção diária não encontrada ou não existente.");
    }

    @Test
    @DisplayName("Deve buscar todas as excecoes de um usuario em determinada data")
    void deveBuscarPorUsuarioEData() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        LocalDate data = LocalDate.of(2026, 10, 15);
        ExcecaoDiaria excecao = ExcecaoDiaria.builder().id(UUID.randomUUID()).build();

        when(repository.findAllByUsuarioAndDataExcecao(usuario, data)).thenReturn(List.of(excecao));

        List<ExcecaoDiaria> resultado = finder.findAllByUsuarioEData(usuario, data);

        assertThat(resultado).hasSize(1);
    }
}
