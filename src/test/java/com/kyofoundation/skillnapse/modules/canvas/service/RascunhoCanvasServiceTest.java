package com.kyofoundation.skillnapse.modules.canvas.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.entity.RascunhoCanvas;
import com.kyofoundation.skillnapse.modules.canvas.finder.RascunhoCanvasFinder;
import com.kyofoundation.skillnapse.modules.canvas.repository.RascunhoCanvasRepository;
import com.kyofoundation.skillnapse.modules.canvas.validator.RascunhoCanvasValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.TopicoFinder;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RascunhoCanvasServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private TopicoFinder topicoFinder;

    @Mock
    private RascunhoCanvasFinder rascunhoCanvasFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private RascunhoCanvasValidator rascunhoCanvasValidator;

    @Mock
    private RascunhoCanvasRepository rascunhoCanvasRepository;

    @InjectMocks
    private RascunhoCanvasService rascunhoCanvasService;

    @Test
    @DisplayName("Deve salvar novo rascunho de canvas com sucesso quando nao existir anterior")
    void deveSalvarNovoRascunhoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        String json = "{\"paths\":[]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(topicoId, json);

        Usuario usuario = Usuario.builder().id(userId).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Tópico Teste").build();
        RascunhoCanvas salvo = RascunhoCanvas.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .topico(topico)
                .dadosDesenhoJson(json)
                .atualizadoEm(Instant.now())
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        when(rascunhoCanvasFinder.findOptionalByUsuarioIdAndTopicoId(userId, topicoId)).thenReturn(Optional.empty());
        when(rascunhoCanvasRepository.save(any(RascunhoCanvas.class))).thenReturn(salvo);

        RascunhoCanvasResponse response = rascunhoCanvasService.salvarRascunho(userId, topicoId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(salvo.getId());
        assertThat(response.topicoId()).isEqualTo(topicoId);
        assertThat(response.dadosDesenhoJson()).isEqualTo(json);

        verify(rascunhoCanvasValidator).validarSalvarRequest(request, topicoId);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(rascunhoCanvasValidator).validarTopicoPertenceUsuario(usuario, topico);
        verify(rascunhoCanvasRepository).save(any(RascunhoCanvas.class));
    }

    @Test
    @DisplayName("Deve atualizar rascunho de canvas existente de forma idempotente (upsert)")
    void deveAtualizarRascunhoExistenteComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        String jsonAntigo = "{\"paths\":[]}";
        String jsonNovo = "{\"paths\":[{\"stroke\":\"#000\"}]}";
        SalvarRascunhoCanvasRequest request = new SalvarRascunhoCanvasRequest(null, jsonNovo);

        Usuario usuario = Usuario.builder().id(userId).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Tópico Teste").build();
        RascunhoCanvas existente = RascunhoCanvas.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .topico(topico)
                .dadosDesenhoJson(jsonAntigo)
                .atualizadoEm(Instant.EPOCH)
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        when(rascunhoCanvasFinder.findOptionalByUsuarioIdAndTopicoId(userId, topicoId)).thenReturn(Optional.of(existente));
        when(rascunhoCanvasRepository.save(existente)).thenReturn(existente);

        RascunhoCanvasResponse response = rascunhoCanvasService.salvarRascunho(userId, topicoId, request);

        assertThat(response).isNotNull();
        assertThat(existente.getDadosDesenhoJson()).isEqualTo(jsonNovo);
        verify(rascunhoCanvasRepository).save(existente);
    }

    @Test
    @DisplayName("Deve obter rascunho de canvas por topico com sucesso")
    void deveObterRascunhoPorTopicoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Topico topico = Topico.builder().id(topicoId).titulo("Tópico Teste").build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .topico(topico)
                .dadosDesenhoJson("{\"paths\":[]}")
                .atualizadoEm(Instant.now())
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        when(rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(userId, topicoId)).thenReturn(rascunho);

        RascunhoCanvasResponse response = rascunhoCanvasService.obterRascunhoPorTopico(userId, topicoId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(rascunho.getId());
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(rascunhoCanvasValidator).validarTopicoPertenceUsuario(usuario, topico);
    }

    @Test
    @DisplayName("Deve obter rascunho de canvas por ID com sucesso")
    void deveObterRascunhoPorIdComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder()
                .id(canvasId)
                .usuario(usuario)
                .dadosDesenhoJson("{\"paths\":[]}")
                .atualizadoEm(Instant.now())
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(rascunhoCanvasFinder.findById(canvasId)).thenReturn(rascunho);

        RascunhoCanvasResponse response = rascunhoCanvasService.obterRascunhoPorId(userId, canvasId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(canvasId);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(rascunhoCanvasValidator).validarPropriedadeCanvas(usuario, rascunho);
    }

    @Test
    @DisplayName("Deve listar rascunhos de canvas paginados")
    void deveListarRascunhosPaginados() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Pageable pageable = PageRequest.of(0, 10);
        RascunhoCanvas rascunho = RascunhoCanvas.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dadosDesenhoJson("{\"paths\":[]}")
                .atualizadoEm(Instant.now())
                .build();
        Page<RascunhoCanvas> pagina = new PageImpl<>(List.of(rascunho), pageable, 1);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(rascunhoCanvasFinder.buscarPorUsuario(userId, pageable)).thenReturn(pagina);

        Page<RascunhoCanvasResponse> resultado = rascunhoCanvasService.listarRascunhos(userId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
    }

    @Test
    @DisplayName("Deve apagar rascunho de canvas por topico")
    void deveApagarRascunhoPorTopico() {
        UUID userId = UUID.randomUUID();
        UUID topicoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Topico topico = Topico.builder().id(topicoId).build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder().id(UUID.randomUUID()).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(topicoFinder.findById(topicoId)).thenReturn(topico);
        when(rascunhoCanvasFinder.findByUsuarioIdAndTopicoId(userId, topicoId)).thenReturn(rascunho);

        rascunhoCanvasService.apagarRascunhoPorTopico(userId, topicoId);

        verify(rascunhoCanvasValidator).validarTopicoPertenceUsuario(usuario, topico);
        verify(rascunhoCanvasRepository).delete(rascunho);
    }

    @Test
    @DisplayName("Deve apagar rascunho de canvas por ID")
    void deveApagarRascunhoPorId() {
        UUID userId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        RascunhoCanvas rascunho = RascunhoCanvas.builder().id(canvasId).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(rascunhoCanvasFinder.findById(canvasId)).thenReturn(rascunho);

        rascunhoCanvasService.apagarRascunhoPorId(userId, canvasId);

        verify(rascunhoCanvasValidator).validarPropriedadeCanvas(usuario, rascunho);
        verify(rascunhoCanvasRepository).delete(rascunho);
    }
}
