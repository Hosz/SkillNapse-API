package com.kyofoundation.skillnapse.modules.questao.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarSimuladoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import com.kyofoundation.skillnapse.modules.questao.enums.TipoSimulado;
import com.kyofoundation.skillnapse.modules.questao.finder.SimuladoFinder;
import com.kyofoundation.skillnapse.modules.questao.finder.TentativaQuestaoFinder;
import com.kyofoundation.skillnapse.modules.questao.repository.SimuladoRepository;
import com.kyofoundation.skillnapse.modules.questao.validator.SimuladoValidator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimuladoServiceTest {

    @Mock
    private SimuladoRepository simuladoRepository;

    @Mock
    private SimuladoFinder simuladoFinder;

    @Mock
    private SimuladoValidator simuladoValidator;

    @Mock
    private TentativaQuestaoFinder tentativaQuestaoFinder;

    @Mock
    private UserFinder userFinder;

    @InjectMocks
    private SimuladoService simuladoService;

    @Test
    @DisplayName("Deve criar simulado com sucesso")
    void deveCriarSimuladoComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        CriarSimuladoRequest request = new CriarSimuladoRequest("Simulado TCU", TipoSimulado.MANUAL);

        when(userFinder.findById(userId)).thenReturn(usuario);
        doNothing().when(simuladoValidator).validarCriacao(request);

        Simulado simuladoSalvo = Simulado.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .titulo("Simulado TCU")
                .tipo(TipoSimulado.MANUAL)
                .concluido(false)
                .criadoEm(Instant.now())
                .tentativas(new ArrayList<>())
                .build();

        when(simuladoRepository.save(any(Simulado.class))).thenReturn(simuladoSalvo);

        SimuladoResponse response = simuladoService.criar(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(simuladoSalvo.getId());
        assertThat(response.titulo()).isEqualTo("Simulado TCU");
        assertThat(response.concluido()).isFalse();
    }

    @Test
    @DisplayName("Deve listar simulados do usuário com métricas consolidadas")
    void deveListarSimuladosDoUsuario() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Pageable pageable = PageRequest.of(0, 10);

        UUID simId = UUID.randomUUID();
        Simulado sim = Simulado.builder()
                .id(simId)
                .titulo("Simulado 1")
                .tipo(TipoSimulado.MANUAL)
                .concluido(false)
                .criadoEm(Instant.now())
                .build();
        Page<Simulado> pagina = new PageImpl<>(List.of(sim));

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(simuladoFinder.findByUsuario(usuario, pageable)).thenReturn(pagina);
        when(tentativaQuestaoFinder.contarPorSimulado(simId)).thenReturn(20L);
        when(tentativaQuestaoFinder.contarAcertosPorSimulado(simId)).thenReturn(15L);

        Page<SimuladoResponse> resultado = simuladoService.listarPorUsuario(userId, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        SimuladoResponse s = resultado.getContent().getFirst();
        assertThat(s.totalQuestoesRespondidas()).isEqualTo(20L);
        assertThat(s.totalAcertos()).isEqualTo(15L);
        assertThat(s.percentualAcerto()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Deve buscar simulado por ID")
    void deveBuscarPorId() {
        UUID userId = UUID.randomUUID();
        UUID simId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Simulado sim = Simulado.builder()
                .id(simId)
                .usuario(usuario)
                .titulo("Simulado 1")
                .tipo(TipoSimulado.MANUAL)
                .concluido(false)
                .criadoEm(Instant.now())
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(simuladoFinder.findById(simId)).thenReturn(sim);
        doNothing().when(simuladoValidator).validarPropriedade(usuario, sim);
        when(tentativaQuestaoFinder.contarPorSimulado(simId)).thenReturn(10L);
        when(tentativaQuestaoFinder.contarAcertosPorSimulado(simId)).thenReturn(8L);

        SimuladoResponse response = simuladoService.buscarPorId(simId, userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(simId);
        assertThat(response.totalAcertos()).isEqualTo(8L);
    }

    @Test
    @DisplayName("Deve concluir simulado com sucesso")
    void deveConcluirSimuladoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID simId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Simulado sim = Simulado.builder()
                .id(simId)
                .usuario(usuario)
                .titulo("Simulado Final")
                .tipo(TipoSimulado.MANUAL)
                .concluido(false)
                .criadoEm(Instant.now())
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(simuladoFinder.findById(simId)).thenReturn(sim);
        doNothing().when(simuladoValidator).validarPropriedade(usuario, sim);
        doNothing().when(simuladoValidator).validarConclusao(sim);

        when(simuladoRepository.save(sim)).thenReturn(sim);
        when(tentativaQuestaoFinder.contarPorSimulado(simId)).thenReturn(5L);
        when(tentativaQuestaoFinder.contarAcertosPorSimulado(simId)).thenReturn(5L);

        SimuladoResponse response = simuladoService.concluir(simId, userId);

        assertThat(response).isNotNull();
        assertThat(response.concluido()).isTrue();
        assertThat(response.percentualAcerto()).isEqualTo(100.0);
    }
}
