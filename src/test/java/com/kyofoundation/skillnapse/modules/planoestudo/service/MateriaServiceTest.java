package com.kyofoundation.skillnapse.modules.planoestudo.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.CriarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EditarMateriaRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.MateriaResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.validator.MateriaValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.validator.PlanoEstudoValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private PlanoEstudoFinder planoEstudoFinder;

    @Mock
    private MateriaFinder materiaFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private PlanoEstudoValidator planoEstudoValidator;

    @Mock
    private MateriaValidator materiaValidator;

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private MateriaService materiaService;

    @Test
    @DisplayName("Deve criar materia com sucesso")
    void deveCriarMateriaComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).usuario(usuario).build();
        CriarMateriaRequest request = new CriarMateriaRequest("Direito Penal", "#EF4444", 1);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarPlanoPertenceUsuario(usuario, plano);
        doNothing().when(materiaValidator).validarCriacao(request);

        MateriaResponse response = materiaService.criarMateria(userId, planoId, request);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Direito Penal");
        assertThat(response.corHex()).isEqualTo("#EF4444");
        verify(materiaRepository).save(any(Materia.class));
    }

    @Test
    @DisplayName("Deve visualizar materia com sucesso")
    void deveVisualizarMateriaComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).nome("Direito Tributário").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);

        MateriaResponse response = materiaService.verMateria(userId, materiaId);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Direito Tributário");
    }

    @Test
    @DisplayName("Deve editar materia com sucesso")
    void deveEditarMateriaComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).nome("Nome Antigo").corHex("#000000").build();
        EditarMateriaRequest request = new EditarMateriaRequest("Nome Novo", "#FFFFFF", 2);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);
        doNothing().when(materiaValidator).validarEdicao(request);

        MateriaResponse response = materiaService.editarMateria(userId, materiaId, request);

        assertThat(response).isNotNull();
        assertThat(materia.getNome()).isEqualTo("Nome Novo");
        assertThat(materia.getCorHex()).isEqualTo("#FFFFFF");
        verify(materiaRepository).save(materia);
    }

    @Test
    @DisplayName("Deve apagar materia com sucesso")
    void deveApagarMateriaComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        Materia materia = Materia.builder().id(materiaId).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(materiaValidator).validarMateriaPertenceUsuario(usuario, materia);

        materiaService.apagarMateria(userId, materiaId);

        verify(materiaRepository).delete(materia);
    }

    @Test
    @DisplayName("Deve listar materias paginadas com sucesso")
    void deveListarMateriasComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).usuario(usuario).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Processo Civil").build();
        Page<Materia> page = new PageImpl<>(List.of(materia));

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarPlanoPertenceUsuario(usuario, plano);
        when(materiaRepository.findByPlanoEstudo(plano, pageable)).thenReturn(page);

        Page<MateriaResponse> response = materiaService.listarMaterias(userId, planoId, pageable);

        assertThat(response).isNotEmpty();
        assertThat(response.getContent().getFirst().nome()).isEqualTo("Processo Civil");
    }
}
