package com.kyofoundation.skillnapse.modules.gamificacao.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import com.kyofoundation.skillnapse.modules.gamificacao.finder.OfensivaUsuarioFinder;
import com.kyofoundation.skillnapse.modules.gamificacao.repository.OfensivaUsuarioRepository;
import com.kyofoundation.skillnapse.modules.gamificacao.support.CalculoOfensivaSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfensivaServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private OfensivaUsuarioFinder ofensivaUsuarioFinder;

    @Mock
    private OfensivaUsuarioRepository ofensivaUsuarioRepository;

    @Mock
    private CalculoOfensivaSupport calculoOfensivaSupport;

    @InjectMocks
    private OfensivaService ofensivaService;

    private Usuario usuario;
    private OfensivaUsuario ofensiva;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(UUID.randomUUID()).build();
        ofensiva = OfensivaUsuario.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .diasConsecutivosAtual(3)
                .maiorSequenciaDias(5)
                .dataUltimoEstudo(LocalDate.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("[obterStatusOfensiva] Deve consultar status e salvar se houver quebra de streak")
    void deveObterStatusESalvarSeHouverQuebra() {
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(ofensivaUsuarioFinder.buscarOuCriar(usuario)).thenReturn(ofensiva);
        when(calculoOfensivaSupport.revalidarStreak(eq(ofensiva), any(LocalDate.class))).thenReturn(true);
        when(ofensivaUsuarioRepository.save(ofensiva)).thenReturn(ofensiva);
        when(calculoOfensivaSupport.isEstudouHoje(eq(ofensiva), any(LocalDate.class))).thenReturn(false);
        when(calculoOfensivaSupport.isOfensivaAtiva(eq(ofensiva), any(LocalDate.class))).thenReturn(false);

        StatusOfensivaResponse response = ofensivaService.obterStatusOfensiva(usuario.getId());

        assertThat(response).isNotNull();
        assertThat(response.usuarioId()).isEqualTo(usuario.getId());
        verify(ofensivaUsuarioRepository).save(ofensiva);
    }

    @Test
    @DisplayName("[obterStatusOfensiva] Não deve salvar no banco se não houver quebra de streak")
    void naoDeveSalvarSeNaoHouverQuebra() {
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(ofensivaUsuarioFinder.buscarOuCriar(usuario)).thenReturn(ofensiva);
        when(calculoOfensivaSupport.revalidarStreak(eq(ofensiva), any(LocalDate.class))).thenReturn(false);
        when(calculoOfensivaSupport.isEstudouHoje(eq(ofensiva), any(LocalDate.class))).thenReturn(false);
        when(calculoOfensivaSupport.isOfensivaAtiva(eq(ofensiva), any(LocalDate.class))).thenReturn(true);

        StatusOfensivaResponse response = ofensivaService.obterStatusOfensiva(usuario.getId());

        assertThat(response).isNotNull();
        assertThat(response.ofensivaAtiva()).isTrue();
        verify(ofensivaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("[registrarEstudo] Deve registrar avanço na ofensiva e persistir")
    void deveRegistrarAvancoNaOfensiva() {
        LocalDate hoje = LocalDate.now();
        when(userFinder.findById(usuario.getId())).thenReturn(usuario);
        when(ofensivaUsuarioFinder.buscarOuCriar(usuario)).thenReturn(ofensiva);
        doNothing().when(calculoOfensivaSupport).registrarEstudo(ofensiva, hoje);
        when(ofensivaUsuarioRepository.save(ofensiva)).thenReturn(ofensiva);
        when(calculoOfensivaSupport.isEstudouHoje(eq(ofensiva), any(LocalDate.class))).thenReturn(true);
        when(calculoOfensivaSupport.isOfensivaAtiva(eq(ofensiva), any(LocalDate.class))).thenReturn(true);

        StatusOfensivaResponse response = ofensivaService.registrarEstudo(usuario.getId(), hoje);

        assertThat(response).isNotNull();
        assertThat(response.estudouHoje()).isTrue();
        verify(calculoOfensivaSupport).registrarEstudo(ofensiva, hoje);
        verify(ofensivaUsuarioRepository).save(ofensiva);
    }

    @Test
    @DisplayName("[registrarEstudoSilencioso] Deve registrar estudo e persistir silenciosamente")
    void deveRegistrarEstudoSilencioso() {
        LocalDate hoje = LocalDate.now();
        when(ofensivaUsuarioFinder.buscarOuCriar(usuario)).thenReturn(ofensiva);
        doNothing().when(calculoOfensivaSupport).registrarEstudo(ofensiva, hoje);
        when(ofensivaUsuarioRepository.save(ofensiva)).thenReturn(ofensiva);

        ofensivaService.registrarEstudoSilencioso(usuario, hoje);

        verify(calculoOfensivaSupport).registrarEstudo(ofensiva, hoje);
        verify(ofensivaUsuarioRepository).save(ofensiva);
    }
}
