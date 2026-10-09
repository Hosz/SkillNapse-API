package com.kyofoundation.skillnapse.modules.gamificacao.finder;

import com.kyofoundation.skillnapse.common.exception.ResourceNotFoundException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.MetaDiaria;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.MetaDiariaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDiariaFinderTest {

    @Mock
    private MetaDiariaRepository repository;

    private MetaDiariaFinder finder;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        finder = new MetaDiariaFinder(repository);
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
    }

    @Test
    @DisplayName("[buscarPorUsuario] Deve retornar metas existentes")
    void deveRetornarMetasExistentes() {
        MetaDiaria meta = MetaDiaria.builder().id(UUID.randomUUID()).usuario(usuario).build();
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(meta));

        MetaDiaria resultado = finder.buscarPorUsuario(usuario);
        assertThat(resultado).isEqualTo(meta);
    }

    @Test
    @DisplayName("[buscarPorUsuario] Deve lançar ResourceNotFoundException se não encontrar")
    void deveLancarResourceNotFoundSeNaoExistir() {
        when(repository.findByUsuario(usuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> finder.buscarPorUsuario(usuario))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("não encontradas");
    }

    @Test
    @DisplayName("[buscarOuCriar] Deve retornar existente quando já cadastrada")
    void deveRetornarExistenteEmBuscarOuCriar() {
        MetaDiaria meta = MetaDiaria.builder().id(UUID.randomUUID()).usuario(usuario).build();
        when(repository.findByUsuario(usuario)).thenReturn(Optional.of(meta));

        MetaDiaria resultado = finder.buscarOuCriar(usuario);
        assertThat(resultado).isEqualTo(meta);
    }

    @Test
    @DisplayName("[buscarOuCriar] Deve criar nova meta com padrões (120min, 15questões) se não existir")
    void deveCriarNovaMetaComPadroesSeNaoExistir() {
        when(repository.findByUsuario(usuario)).thenReturn(Optional.empty());
        MetaDiaria nova = MetaDiaria.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .metaMinutosEstudo(120)
                .metaQuestoesResolvidas(15)
                .build();
        when(repository.save(any(MetaDiaria.class))).thenReturn(nova);

        MetaDiaria resultado = finder.buscarOuCriar(usuario);
        assertThat(resultado).isEqualTo(nova);
        verify(repository).save(any(MetaDiaria.class));
    }
}
