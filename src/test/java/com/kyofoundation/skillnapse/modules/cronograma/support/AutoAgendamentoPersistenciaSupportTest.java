package com.kyofoundation.skillnapse.modules.cronograma.support;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.BlocoParaAplicarRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.enums.TipoBloco;
import com.kyofoundation.skillnapse.modules.cronograma.finder.TemplateSemanalFinder;
import com.kyofoundation.skillnapse.modules.cronograma.repository.BlocoHorarioTemplateRepository;
import com.kyofoundation.skillnapse.modules.cronograma.repository.TemplateSemanalRepository;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoAgendamentoPersistenciaSupportTest {

    @Mock
    private TemplateSemanalRepository templateSemanalRepository;

    @Mock
    private BlocoHorarioTemplateRepository blocoHorarioTemplateRepository;

    @Mock
    private TemplateSemanalFinder templateSemanalFinder;

    @InjectMocks
    private AutoAgendamentoPersistenciaSupport persistenciaSupport;

    @Test
    @DisplayName("Deve persistir grade criando novo template quando templateSemanalId for nulo")
    void devePersistirCriandoNovoTemplate() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        UUID planoId = UUID.randomUUID();
        UUID matId = UUID.randomUUID();
        Materia materia = Materia.builder().id(matId).nome("Língua Portuguesa").build();

        BlocoParaAplicarRequest bloco = new BlocoParaAplicarRequest(
                DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(9, 0), TipoBloco.FOCO_TEORIA, matId, null, "Base"
        );
        AplicarAutoAgendamentoRequest request = new AplicarAutoAgendamentoRequest(
                planoId, null, "Meu Cronograma IA", true, "Resumo", List.of(bloco)
        );

        TemplateSemanal templateCriado = TemplateSemanal.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .nome("Meu Cronograma IA")
                .ativo(true)
                .blocosHorario(new ArrayList<>())
                .build();

        when(templateSemanalRepository.save(any(TemplateSemanal.class))).thenReturn(templateCriado);

        ResultadoAutoAgendamentoResponse resultado = persistenciaSupport.persistirBlocosAprovados(
                usuario, request, Map.of(matId, materia), Map.of()
        );

        assertThat(resultado).isNotNull();
        assertThat(resultado.totalBlocosPersistidos()).isEqualTo(1);
        verify(templateSemanalRepository).save(any(TemplateSemanal.class));
        verify(blocoHorarioTemplateRepository).save(any(BlocoHorarioTemplate.class));
    }

    @Test
    @DisplayName("Deve persistir grade atualizando template existente e limpando blocos anteriores")
    void devePersistirAtualizandoTemplateExistente() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        UUID templateId = UUID.randomUUID();
        UUID planoId = UUID.randomUUID();

        BlocoParaAplicarRequest bloco = new BlocoParaAplicarRequest(
                DiaSemana.TERCA, LocalTime.of(14, 0), LocalTime.of(15, 0), TipoBloco.FOCO_TEORIA, null, null, "Estudo"
        );
        AplicarAutoAgendamentoRequest request = new AplicarAutoAgendamentoRequest(
                planoId, templateId, null, true, "Resumo", List.of(bloco)
        );

        List<BlocoHorarioTemplate> blocosAntigos = new ArrayList<>(List.of(
                BlocoHorarioTemplate.builder().id(UUID.randomUUID()).build()
        ));
        TemplateSemanal templateExistente = TemplateSemanal.builder()
                .id(templateId)
                .usuario(usuario)
                .nome("Template Existente")
                .blocosHorario(blocosAntigos)
                .build();

        when(templateSemanalFinder.findById(templateId)).thenReturn(templateExistente);

        ResultadoAutoAgendamentoResponse resultado = persistenciaSupport.persistirBlocosAprovados(
                usuario, request, Map.of(), Map.of()
        );

        assertThat(resultado.templateSemanalId()).isEqualTo(templateId);
        verify(blocoHorarioTemplateRepository).deleteAll(any());
        verify(blocoHorarioTemplateRepository).save(any(BlocoHorarioTemplate.class));
    }
}
