package com.kyofoundation.skillnapse.modules.planoestudo.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.AtualizarProgressoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarTopicoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.TopicoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.enums.NivelProficiencia;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.validator.MateriaValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.validator.TopicoValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicoServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private MateriaFinder materiaFinder;

    @Mock
    private TopicoFinder topicoFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private MateriaValidator materiaValidator;

    @Mock
    private TopicoValidator topicoValidator;

    @Mock
    private TopicoRepository topicoRepository;

    @InjectMocks
    private TopicoService topicoService;

    @Test
    @DisplayName("Deve criar topico com sucesso")
    void deveCriarTopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).build();
        CriarTopicoRequest request = new CriarTopicoRequest("Controle Difuso", null, 2, NivelProficiencia.INICIANTE, 1);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);
        doNothing().when(topicoValidator).validarCriacao(request);

        TopicoResponse response = topicoService.criarTopico(userId, materiaId, request);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Controle Difuso");
        assertThat(response.pesoEdital()).isEqualTo(2);
        assertThat(response.nivelProficiencia()).isEqualTo(NivelProficiencia.INICIANTE);
        verify(topicoRepository).save(any(Topico.class));
    }

    @Test
    @DisplayName("Deve criar subtópico com sucesso quando informado topicoPaiId")
    void deveCriarSubtopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();
        UUID topicoPaiId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).build();
        Topico topicoPai = Topico.builder().id(topicoPaiId).materia(materia).build();
        CriarTopicoRequest request = new CriarTopicoRequest("Subtópico", topicoPaiId, 1, NivelProficiencia.INICIANTE, 0);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        when(topicoFinder.findById(topicoPaiId)).thenReturn(topicoPai);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);
        doNothing().when(topicoValidator).validarCriacao(request);
        doNothing().when(topicoValidator).validarTopicoPaiPertenceMateria(topicoPai, materia);

        TopicoResponse response = topicoService.criarTopico(userId, materiaId, request);

        assertThat(response).isNotNull();
        assertThat(response.topicoPaiId()).isEqualTo(topicoPaiId);
        verify(topicoRepository).save(any(Topico.class));
    }

    @Test
    @DisplayName("Deve visualizar topico com sucesso")
    void deveVisualizarTopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Direitos Fundamentais").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(topicoValidator).validarTopicoPertenceUsuario(usuario, topico);

        TopicoResponse response = topicoService.visualizarTopico(userId, topicoId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(topicoId);
        assertThat(response.titulo()).isEqualTo("Direitos Fundamentais");
    }

    @Test
    @DisplayName("Deve editar topico com sucesso")
    void deveEditarTopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Titulo Antigo").pesoEdital(1).build();
        EditarTopicoRequest request = new EditarTopicoRequest("Titulo Novo", 4, NivelProficiencia.AVANCADO, true, 2);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(topicoValidator).validarTopicoPertenceUsuario(usuario, topico);
        doNothing().when(topicoValidator).validarEdicao(request);

        TopicoResponse response = topicoService.editarTopico(userId, topicoId, request);

        assertThat(response).isNotNull();
        assertThat(topico.getTitulo()).isEqualTo("Titulo Novo");
        assertThat(topico.getPesoEdital()).isEqualTo(4);
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.AVANCADO);
        assertThat(topico.getConcluido()).isTrue();
        verify(topicoRepository).save(topico);
    }

    @Test
    @DisplayName("Deve atualizar status e proficiencia do topico com sucesso")
    void deveAtualizarProgressoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Topico topico = Topico.builder().id(topicoId).concluido(false).nivelProficiencia(NivelProficiencia.INICIANTE).build();
        AtualizarProgressoRequest request = new AtualizarProgressoRequest(true, NivelProficiencia.AVANCADO);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(topicoValidator).validarTopicoPertenceUsuario(usuario, topico);
        doNothing().when(topicoValidator).validarProgresso(request);

        TopicoResponse response = topicoService.atualizarStatusEProficiencia(userId, topicoId, request);

        assertThat(response).isNotNull();
        assertThat(topico.getConcluido()).isTrue();
        assertThat(topico.getNivelProficiencia()).isEqualTo(NivelProficiencia.AVANCADO);
        verify(topicoRepository).save(topico);
    }

    @Test
    @DisplayName("Deve deletar topico com sucesso")
    void deveDeletarTopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Topico topico = Topico.builder().id(topicoId).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(topicoValidator).validarTopicoPertenceUsuario(usuario, topico);

        topicoService.deletarTopico(userId, topicoId);

        verify(topicoRepository).delete(topico);
    }

    @Test
    @DisplayName("Deve listar arvore de topicos da materia com sucesso")
    void deveListarArvoreTopicosComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).build();

        Topico filho = Topico.builder().id(UUID.randomUUID()).titulo("Filho").build();
        Topico pai = Topico.builder().id(UUID.randomUUID()).titulo("Pai").subtopicos(List.of(filho)).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);
        when(topicoRepository.findByMateriaAndTopicoPaiIsNullOrderByOrdemAsc(materia)).thenReturn(List.of(pai));

        List<TopicoResponse> response = topicoService.listarArvoreTopicosPorMateria(userId, materiaId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().titulo()).isEqualTo("Pai");
        assertThat(response.getFirst().subtopicos()).hasSize(1);
        assertThat(response.getFirst().subtopicos().getFirst().titulo()).isEqualTo("Filho");
    }
}
