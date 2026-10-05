package com.kyofoundation.skillnapse.modules.planoestudo.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.PlanoEstudoRepository;
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
class PlanoEstudoServiceTest {

    @Mock
    private PlanoEstudoRepository planoEstudoRepository;

    @Mock
    private PlanoEstudoValidator planoEstudoValidator;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private UserFinder userFinder;

    @Mock
    private PlanoEstudoFinder planoEstudoFinder;

    @InjectMocks
    private PlanoEstudoService planoEstudoService;

    @Test
    @DisplayName("Deve criar plano de estudo com sucesso")
    void deveCriarPlanoComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudoRequest request = new PlanoEstudoRequest("Polícia Federal", "Foco Escrivão");

        when(userFinder.findById(userId)).thenReturn(usuario);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarCriacao(request);

        PlanoEstudoResponse response = planoEstudoService.criarPlano(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Polícia Federal");
        assertThat(response.descricao()).isEqualTo("Foco Escrivão");
        verify(planoEstudoRepository).save(any(PlanoEstudo.class));
    }

    @Test
    @DisplayName("Deve visualizar plano de estudo com sucesso")
    void deveVisualizarPlanoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).titulo("Receita Federal").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarPlanoPertenceUsuario(usuario, plano);

        PlanoEstudoResponse response = planoEstudoService.visualizarPlano(userId, planoId);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Receita Federal");
    }

    @Test
    @DisplayName("Deve editar plano de estudo com sucesso")
    void deveEditarPlanoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).titulo("Titulo Velho").descricao("Descricao Velha").build();
        EdicaoPlanoEstudoRequest request = new EdicaoPlanoEstudoRequest("Titulo Atualizado", "Descricao Atualizada", false);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarPlanoPertenceUsuario(usuario, plano);
        doNothing().when(planoEstudoValidator).validarEdicao(request);

        PlanoEstudoResponse response = planoEstudoService.editarPlano(userId, planoId, request);

        assertThat(response).isNotNull();
        assertThat(plano.getTitulo()).isEqualTo("Titulo Atualizado");
        assertThat(plano.getDescricao()).isEqualTo("Descricao Atualizada");
        assertThat(plano.getAtivo()).isFalse();
        verify(planoEstudoRepository).save(plano);
    }

    @Test
    @DisplayName("Deve apagar plano de estudo com sucesso")
    void deveApagarPlanoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        doNothing().when(planoEstudoValidator).validarPlanoPertenceUsuario(usuario, plano);

        planoEstudoService.apagarPlano(userId, planoId);

        verify(planoEstudoRepository).delete(plano);
    }

    @Test
    @DisplayName("Deve listar planos paginados do usuario com sucesso")
    void deveListarPlanosComSucesso() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = Usuario.builder().id(userId).ativo(true).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).titulo("Plano 1").build();
        Page<PlanoEstudo> page = new PageImpl<>(List.of(plano));

        when(userFinder.findById(userId)).thenReturn(usuario);
        doNothing().when(usuarioValidator).validarUsuarioAtivo(usuario);
        when(planoEstudoRepository.findAllByUsuario(usuario, pageable)).thenReturn(page);

        Page<PlanoEstudoResponse> response = planoEstudoService.listarPlanos(userId, pageable);

        assertThat(response).isNotEmpty();
        assertThat(response.getContent().getFirst().titulo()).isEqualTo("Plano 1");
    }
}
