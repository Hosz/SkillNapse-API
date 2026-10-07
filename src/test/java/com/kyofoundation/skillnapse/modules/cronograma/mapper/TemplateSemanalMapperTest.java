package com.kyofoundation.skillnapse.modules.cronograma.mapper;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.GradeSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateSemanalMapperTest {

    @Test
    @DisplayName("Deve mapear criacao de template semanal corretamente")
    void deveMapearCriacaoCorretamente() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        CriarTemplateSemanalRequest request = new CriarTemplateSemanalRequest("Rotina Padrão", true);

        TemplateSemanal template = TemplateSemanalMapper.toCriarTemplateSemanal(usuario, request);

        assertThat(template.getNome()).isEqualTo("Rotina Padrão");
        assertThat(template.getAtivo()).isTrue();
        assertThat(template.getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("Deve mapear para response com total de blocos")
    void deveMapearParaResponse() {
        TemplateSemanal template = TemplateSemanal.builder()
                .id(UUID.randomUUID())
                .nome("Rotina Foco")
                .ativo(true)
                .build();

        TemplateSemanalResponse response = TemplateSemanalMapper.toResponse(template);

        assertThat(response.id()).isEqualTo(template.getId());
        assertThat(response.nome()).isEqualTo("Rotina Foco");
        assertThat(response.totalBlocos()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve mapear grade semanal com dias ordenados e blocos agrupados")
    void deveMapearGradeSemanal() {
        UUID templateId = UUID.randomUUID();
        TemplateSemanal template = TemplateSemanal.builder()
                .id(templateId)
                .nome("Matriz Semanal")
                .ativo(true)
                .build();

        BlocoHorarioTemplate bloco1 = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(9, 30))
                .build();

        BlocoHorarioTemplate bloco2 = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 0))
                .build();

        GradeSemanalResponse gradeResponse = TemplateSemanalMapper.toResponseGradeSemanal(template, List.of(bloco2, bloco1));

        assertThat(gradeResponse.templateId()).isEqualTo(templateId);
        assertThat(gradeResponse.grade()).containsKey(DiaSemana.SEGUNDA);
        assertThat(gradeResponse.grade()).containsKey(DiaSemana.DOMINGO);
        assertThat(gradeResponse.grade().get(DiaSemana.SEGUNDA)).hasSize(2);
        // Garante ordenação cronológica pelo horário de início
        assertThat(gradeResponse.grade().get(DiaSemana.SEGUNDA).get(0).horaInicio()).isEqualTo(LocalTime.of(8, 0));
    }
}
