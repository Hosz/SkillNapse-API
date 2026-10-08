package com.kyofoundation.skillnapse.modules.cronograma.service;

import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.JanelaDisponibilidadeRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.AutoAgendamentoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.dto.structure.BlocoIaEstruturado;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.EstrategiaAgendamento;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.finder.TemplateSemanalFinder;
import com.kyofoundation.skillnapse.modules.cronograma.support.AutoAgendamentoPersistenciaSupport;
import com.kyofoundation.skillnapse.modules.cronograma.support.AutoAgendamentoPromptSupport;
import com.kyofoundation.skillnapse.modules.cronograma.validator.AutoAgendamentoValidator;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.finder.PlanoEstudoFinder;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.MateriaRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.repository.TopicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoAgendamentoServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private PlanoEstudoFinder planoEstudoFinder;

    @Mock
    private TemplateSemanalFinder templateSemanalFinder;

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private TopicoRepository topicoRepository;

    @Mock
    private AutoAgendamentoValidator autoAgendamentoValidator;

    @Mock
    private AutoAgendamentoPromptSupport autoAgendamentoPromptSupport;

    @Mock
    private AutoAgendamentoPersistenciaSupport autoAgendamentoPersistenciaSupport;

    @Mock
    private AiOrchestratorService aiOrchestratorService;

    @InjectMocks
    private AutoAgendamentoService autoAgendamentoService;

    @Test
    @DisplayName("Deve gerar sugestao de auto-agendamento com sucesso")
    void deveGerarSugestaoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();
        UUID matId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).usuario(usuario).titulo("Concurso").build();
        Materia materia = Materia.builder().id(matId).nome("Português").build();

        JanelaDisponibilidadeRequest janela = new JanelaDisponibilidadeRequest(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(21, 0)
        );
        GerarAutoAgendamentoRequest request = new GerarAutoAgendamentoRequest(
                planoId, List.of(janela), 60, 10, true, true, EstrategiaAgendamento.FOCO_PESO_EDITAL
        );

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        when(materiaRepository.findByPlanoEstudoOrderByOrdemAsc(plano)).thenReturn(List.of(materia));
        when(topicoRepository.findByMateriaIn(List.of(materia))).thenReturn(Collections.emptyList());

        PromptRequest promptRequest = new PromptRequest("user prompt", "system prompt", 0.2, 4000);
        when(autoAgendamentoPromptSupport.criarPrompt(eq(plano), any(), any(), eq(request))).thenReturn(promptRequest);

        BlocoIaEstruturado blocoIa = new BlocoIaEstruturado(
                DiaSemana.SEGUNDA, LocalTime.of(19, 0), LocalTime.of(20, 0), TipoBloco.FOCO_TEORIA, matId, null, "Justificativa"
        );
        AutoAgendamentoIaEstruturado iaResponse = new AutoAgendamentoIaEstruturado("Resumo pedagógico", List.of(blocoIa));

        when(aiOrchestratorService.generateStructured(promptRequest, AutoAgendamentoIaEstruturado.class))
                .thenReturn(iaResponse);

        SugestaoAutoAgendamentoResponse response = autoAgendamentoService.gerarSugestao(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.planoEstudoId()).isEqualTo(planoId);
        assertThat(response.totalBlocosSugeridos()).isEqualTo(1);
        assertThat(response.resumoPedagogico()).isEqualTo("Resumo pedagógico");

        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(autoAgendamentoValidator).validarRequisicao(request);
        verify(autoAgendamentoValidator).validarPlano(plano, usuario, List.of(materia));
    }

    @Test
    @DisplayName("Deve aplicar auto-agendamento e persistir grade com sucesso")
    void deveAplicarAgendamentoComSucesso() {
        UUID userId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder().id(userId).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(planoId).usuario(usuario).titulo("Concurso").build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).nome("Raciocínio Lógico").build();

        com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest bloco =
                new com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest(
                        DiaSemana.TERCA, LocalTime.of(8, 0), LocalTime.of(9, 0), TipoBloco.FOCO_TEORIA, null, null, "Estudo"
                );
        AplicarAutoAgendamentoRequest request = new AplicarAutoAgendamentoRequest(
                planoId, null, "Novo Template", true, "Resumo", List.of(bloco)
        );

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(planoEstudoFinder.findById(planoId)).thenReturn(plano);
        when(materiaRepository.findByPlanoEstudoOrderByOrdemAsc(plano)).thenReturn(List.of(materia));
        when(topicoRepository.findByMateriaIn(List.of(materia))).thenReturn(Collections.emptyList());

        ResultadoAutoAgendamentoResponse resultadoMock = new ResultadoAutoAgendamentoResponse(
                UUID.randomUUID(), "Novo Template", 1, "Resumo", Collections.emptyList()
        );
        when(autoAgendamentoPersistenciaSupport.persistirBlocosAprovados(eq(usuario), eq(request), any(), any()))
                .thenReturn(resultadoMock);

        ResultadoAutoAgendamentoResponse response = autoAgendamentoService.aplicarAgendamento(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.nomeTemplate()).isEqualTo("Novo Template");
        verify(usuarioValidator).validarUsuarioAtivo(usuario);
        verify(autoAgendamentoValidator).validarAplicacaoRequest(request);
        verify(autoAgendamentoValidator).validarBlocosParaAplicacao(eq(request.blocos()), any(), any());
        verify(autoAgendamentoPersistenciaSupport).persistirBlocosAprovados(eq(usuario), eq(request), any(), any());
    }
}
