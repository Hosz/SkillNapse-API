package com.kyofoundation.skillnapse.modules.questao.service;

import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoDetalheResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.response.QuestaoResumoResponse;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import com.kyofoundation.skillnapse.modules.questao.finder.QuestaoFinder;
import com.kyofoundation.skillnapse.modules.questao.repository.QuestaoRepository;
import com.kyofoundation.skillnapse.modules.questao.support.HashEnunciadoSupport;
import com.kyofoundation.skillnapse.modules.questao.validator.QuestaoValidator;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private QuestaoFinder questaoFinder;

    @Mock
    private QuestaoValidator questaoValidator;

    @Mock
    private HashEnunciadoSupport hashEnunciadoSupport;

    @InjectMocks
    private QuestaoService questaoService;

    @Test
    @DisplayName("Deve criar questão com sucesso gerando hash e salvando")
    void deveCriarQuestaoComSucesso() {
        CriarQuestaoRequest request = new CriarQuestaoRequest(
                "Direito Administrativo",
                "Poderes",
                "O poder de polícia é indelegável a particulares.",
                "STF Tema 532.",
                DificuldadeQuestao.DIFICIL,
                "CESPE",
                2024,
                List.of(
                        new CriarAlternativaRequest("A", "Certo", true),
                        new CriarAlternativaRequest("B", "Errado", false)
                )
        );

        when(hashEnunciadoSupport.gerarHash(request.enunciado())).thenReturn("hashValido");
        doNothing().when(questaoValidator).validarCriacao(request, "hashValido");

        Questao questaoSalva = Questao.builder()
                .id(UUID.randomUUID())
                .assuntoGeral(request.assuntoGeral())
                .topicoReferencia(request.topicoReferencia())
                .enunciado(request.enunciado())
                .explicacaoGabarito(request.explicacaoGabarito())
                .dificuldade(request.dificuldade())
                .alternativas(new ArrayList<>())
                .build();

        when(questaoRepository.save(any(Questao.class))).thenReturn(questaoSalva);

        QuestaoDetalheResponse response = questaoService.criar(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(questaoSalva.getId());
        assertThat(response.assuntoGeral()).isEqualTo("Direito Administrativo");
        verify(questaoValidator).validarCriacao(request, "hashValido");
        verify(questaoRepository).save(any(Questao.class));
    }

    @Test
    @DisplayName("Deve buscar questões com filtros de forma paginada")
    void deveBuscarComFiltrosPaginada() {
        Pageable pageable = PageRequest.of(0, 10);
        Questao q = Questao.builder()
                .id(UUID.randomUUID())
                .assuntoGeral("Civil")
                .topicoReferencia("Contratos")
                .enunciado("Enunciado")
                .dificuldade(DificuldadeQuestao.MEDIA)
                .alternativas(new ArrayList<>())
                .build();
        Page<Questao> pagina = new PageImpl<>(List.of(q));

        when(questaoFinder.buscarComFiltros("Civil", null, null, null, null, null, pageable))
                .thenReturn(pagina);

        Page<QuestaoResumoResponse> resultado = questaoService.buscarComFiltros("Civil", null, null, null, null, null, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().assuntoGeral()).isEqualTo("Civil");
    }

    @Test
    @DisplayName("Deve buscar questão por ID com detalhes e alternativas")
    void deveBuscarPorIdComDetalhes() {
        UUID id = UUID.randomUUID();
        Questao q = Questao.builder()
                .id(id)
                .assuntoGeral("Direito Penal")
                .topicoReferencia("Tipicidade")
                .enunciado("O crime culposo...")
                .explicacaoGabarito("Art 18, II do CP")
                .dificuldade(DificuldadeQuestao.MEDIA)
                .alternativas(new ArrayList<>())
                .build();

        when(questaoFinder.findByIdComAlternativas(id)).thenReturn(q);

        QuestaoDetalheResponse response = questaoService.buscarPorId(id);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.assuntoGeral()).isEqualTo("Direito Penal");
    }
}
