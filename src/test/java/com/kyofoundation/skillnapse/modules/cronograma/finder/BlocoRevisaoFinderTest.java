package com.kyofoundation.skillnapse.modules.cronograma.finder;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.ExcecaoDiaria;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlocoRevisaoFinderTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private TemplateSemanalFinder templateSemanalFinder;

    @Mock
    private BlocoHorarioTemplateFinder blocoHorarioTemplateFinder;

    @Mock
    private ExcecaoDiariaFinder excecaoDiariaFinder;

    @Mock
    private MateriaFinder materiaFinder;

    @Mock
    private TopicoFinder topicoFinder;

    private BlocoRevisaoFinder finder;

    @BeforeEach
    void setUp() {
        finder = new BlocoRevisaoFinder(
                userFinder,
                templateSemanalFinder,
                blocoHorarioTemplateFinder,
                excecaoDiariaFinder,
                materiaFinder,
                topicoFinder
        );
    }

    @Test
    @DisplayName("[buscarUsuario] Deve delegar para UserFinder")
    void deveBuscarUsuarioComSucesso() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(id).build();
        when(userFinder.findById(id)).thenReturn(usuario);

        Usuario resultado = finder.buscarUsuario(id);
        assertThat(resultado).isEqualTo(usuario);
        verify(userFinder).findById(id);
    }

    @Test
    @DisplayName("[buscarTemplate] Deve delegar para TemplateSemanalFinder")
    void deveBuscarTemplateComSucesso() {
        UUID id = UUID.randomUUID();
        TemplateSemanal template = TemplateSemanal.builder().id(id).build();
        when(templateSemanalFinder.findById(id)).thenReturn(template);

        TemplateSemanal resultado = finder.buscarTemplate(id);
        assertThat(resultado).isEqualTo(template);
        verify(templateSemanalFinder).findById(id);
    }

    @Test
    @DisplayName("[buscarMateriaOpcional] Deve retornar null quando id for nulo e delegar quando informado")
    void deveBuscarMateriaOpcional() {
        assertThat(finder.buscarMateriaOpcional(null)).isNull();

        UUID id = UUID.randomUUID();
        Materia materia = Materia.builder().id(id).build();
        when(materiaFinder.findById(id)).thenReturn(materia);

        Materia resultado = finder.buscarMateriaOpcional(id);
        assertThat(resultado).isEqualTo(materia);
        verify(materiaFinder).findById(id);
    }

    @Test
    @DisplayName("[buscarTopicos] Deve delegar para TopicoFinder")
    void deveBuscarTopicosComSucesso() {
        UUID id = UUID.randomUUID();
        Topico topico = Topico.builder().id(id).build();
        when(topicoFinder.findAllByIds(List.of(id))).thenReturn(List.of(topico));

        List<Topico> resultado = finder.buscarTopicos(List.of(id));
        assertThat(resultado).containsExactly(topico);
        verify(topicoFinder).findAllByIds(List.of(id));
    }

    @Test
    @DisplayName("[buscarBlocoTemplate] Deve delegar para BlocoHorarioTemplateFinder")
    void deveBuscarBlocoTemplateComSucesso() {
        UUID id = UUID.randomUUID();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder().id(id).build();
        when(blocoHorarioTemplateFinder.findById(id)).thenReturn(bloco);

        BlocoHorarioTemplate resultado = finder.buscarBlocoTemplate(id);
        assertThat(resultado).isEqualTo(bloco);
        verify(blocoHorarioTemplateFinder).findById(id);
    }

    @Test
    @DisplayName("[buscarExcecaoDiaria] Deve delegar para ExcecaoDiariaFinder")
    void deveBuscarExcecaoDiariaComSucesso() {
        UUID id = UUID.randomUUID();
        ExcecaoDiaria excecao = ExcecaoDiaria.builder().id(id).build();
        when(excecaoDiariaFinder.findById(id)).thenReturn(excecao);

        ExcecaoDiaria resultado = finder.buscarExcecaoDiaria(id);
        assertThat(resultado).isEqualTo(excecao);
        verify(excecaoDiariaFinder).findById(id);
    }
}
