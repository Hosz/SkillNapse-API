package com.kyofoundation.skillnapse.modules.flashcard.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.entity.Baralho;
import com.kyofoundation.skillnapse.modules.flashcard.finder.BaralhoFinder;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoMetricasProjection;
import com.kyofoundation.skillnapse.modules.flashcard.repository.BaralhoRepository;
import com.kyofoundation.skillnapse.modules.flashcard.validator.BaralhoValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.MateriaFinder;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaralhoServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private MateriaFinder materiaFinder;

    @Mock
    private BaralhoFinder baralhoFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private BaralhoValidator baralhoValidator;

    @Mock
    private BaralhoRepository baralhoRepository;

    @InjectMocks
    private BaralhoService baralhoService;

    @Test
    @DisplayName("Deve criar baralho com sucesso")
    void deveCriarBaralhoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID materiaId = UUID.randomUUID();
        CriarBaralhoRequest request = new CriarBaralhoRequest("Constitucional", "Artigos 1 a 5", materiaId);

        Usuario usuario = Usuario.builder().id(userId).build();
        Materia materia = Materia.builder().id(materiaId).nome("Direito").build();
        Baralho salvo = Baralho.builder().id(UUID.randomUUID()).usuario(usuario).materia(materia).titulo("Constitucional").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(materiaFinder.findById(materiaId)).thenReturn(materia);
        when(baralhoRepository.save(any(Baralho.class))).thenReturn(salvo);

        BaralhoResponse response = baralhoService.criarBaralho(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo("Constitucional");
        verify(baralhoValidator).validarCriacao(request);
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(baralhoValidator).validarMateriaPertenceUsuario(usuario, materia);
        verify(baralhoRepository).save(any(Baralho.class));
    }

    @Test
    @DisplayName("Deve atualizar baralho existente")
    void deveAtualizarBaralhoExistente() {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        AtualizarBaralhoRequest request = new AtualizarBaralhoRequest("Novo Título", "Nova Descrição", null);

        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().id(baralhoId).usuario(usuario).titulo("Antigo").build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(baralhoFinder.findById(baralhoId)).thenReturn(baralho);
        when(baralhoRepository.save(baralho)).thenReturn(baralho);
        when(baralhoFinder.obterMetricasDoBaralho(eq(baralhoId), any(LocalDate.class))).thenReturn(Optional.empty());

        BaralhoResponse response = baralhoService.atualizarBaralho(userId, baralhoId, request);

        assertThat(response).isNotNull();
        assertThat(baralho.getTitulo()).isEqualTo("Novo Título");
        verify(baralhoValidator).validarAtualizacao(request);
        verify(baralhoValidator).validarPropriedadeBaralho(usuario, baralho);
    }

    @Test
    @DisplayName("Deve obter baralho por ID com metricas")
    void deveObterBaralhoPorId() {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().id(baralhoId).usuario(usuario).titulo("Título").build();

        BaralhoMetricasProjection metricas = mock(BaralhoMetricasProjection.class);
        when(metricas.getTotalCards()).thenReturn(10L);
        when(metricas.getTotalCardsParaRevisar()).thenReturn(3L);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(baralhoFinder.findById(baralhoId)).thenReturn(baralho);
        when(baralhoFinder.obterMetricasDoBaralho(eq(baralhoId), any(LocalDate.class))).thenReturn(Optional.of(metricas));

        BaralhoResponse response = baralhoService.obterBaralho(userId, baralhoId);

        assertThat(response).isNotNull();
        assertThat(response.totalCards()).isEqualTo(10L);
        assertThat(response.totalCardsParaRevisar()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve listar baralhos com metricas agregadas")
    void deveListarBaralhosComMetricas() {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Pageable pageable = PageRequest.of(0, 10);
        Baralho baralho = Baralho.builder().id(baralhoId).usuario(usuario).titulo("Título").build();
        Page<Baralho> pagina = new PageImpl<>(List.of(baralho), pageable, 1);

        BaralhoMetricasProjection metricas = mock(BaralhoMetricasProjection.class);
        when(metricas.getBaralhoId()).thenReturn(baralhoId);
        when(metricas.getTotalCards()).thenReturn(5L);
        when(metricas.getTotalCardsParaRevisar()).thenReturn(2L);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(baralhoFinder.buscarPorUsuario(userId, pageable)).thenReturn(pagina);
        when(baralhoFinder.obterMetricasPorUsuario(eq(userId), any(LocalDate.class))).thenReturn(List.of(metricas));

        Page<BaralhoResponse> resultado = baralhoService.listarBaralhos(userId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent().get(0).totalCards()).isEqualTo(5L);
        assertThat(resultado.getContent().get(0).totalCardsParaRevisar()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve apagar baralho com sucesso")
    void deveApagarBaralho() {
        UUID userId = UUID.randomUUID();
        UUID baralhoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Baralho baralho = Baralho.builder().id(baralhoId).usuario(usuario).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(baralhoFinder.findById(baralhoId)).thenReturn(baralho);

        baralhoService.apagarBaralho(userId, baralhoId);

        verify(baralhoValidator).validarPropriedadeBaralho(usuario, baralho);
        verify(baralhoRepository).delete(baralho);
    }
}
