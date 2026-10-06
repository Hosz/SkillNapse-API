package com.kyofoundation.skillnapse.modules.cronograma.service;

import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.finder.UserFinder;
import com.kyofoundation.skillnapse.modules.auth.validator.UsuarioValidator;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.GradeSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.entity.BlocoHorarioTemplate;
import com.kyofoundation.skillnapse.modules.cronograma.entity.TemplateSemanal;
import com.kyofoundation.skillnapse.modules.cronograma.enums.DiaSemana;
import com.kyofoundation.skillnapse.modules.cronograma.finder.BlocoHorarioTemplateFinder;
import com.kyofoundation.skillnapse.modules.cronograma.finder.TemplateSemanalFinder;
import com.kyofoundation.skillnapse.modules.cronograma.repository.TemplateSemanalRepository;
import com.kyofoundation.skillnapse.modules.cronograma.validator.TemplateSemanalValidator;
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

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateSemanalServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private TemplateSemanalFinder templateSemanalFinder;

    @Mock
    private BlocoHorarioTemplateFinder blocoHorarioTemplateFinder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @Mock
    private TemplateSemanalValidator templateSemanalValidator;

    @Mock
    private TemplateSemanalRepository templateSemanalRepository;

    @InjectMocks
    private TemplateSemanalService templateSemanalService;

    @Test
    @DisplayName("Deve criar template semanal com sucesso")
    void deveCriarTemplateComSucesso() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        CriarTemplateSemanalRequest request = new CriarTemplateSemanalRequest("Rotina Padrão", true);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(templateSemanalRepository.findAllByUsuarioAndAtivoTrue(usuario)).thenReturn(Collections.emptyList());

        TemplateSemanalResponse response = templateSemanalService.criarTemplate(userId, request);

        assertThat(response.nome()).isEqualTo("Rotina Padrão");
        assertThat(response.ativo()).isTrue();
        verify(templateSemanalRepository).save(any(TemplateSemanal.class));
    }

    @Test
    @DisplayName("Deve listar templates semanais paginados")
    void deveListarTemplates() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        Pageable pageable = PageRequest.of(0, 10);
        TemplateSemanal template = TemplateSemanal.builder().id(UUID.randomUUID()).nome("Rotina").ativo(true).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(templateSemanalFinder.findAllByUsuario(usuario, pageable)).thenReturn(new PageImpl<>(List.of(template)));

        Page<TemplateSemanalResponse> result = templateSemanalService.listarTemplates(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Rotina");
    }

    @Test
    @DisplayName("Deve visualizar grade semanal completa com ordenacao por dia e hora")
    void deveVisualizarGradeSemanal() {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        TemplateSemanal template = TemplateSemanal.builder().id(templateId).nome("Grade").ativo(true).build();
        BlocoHorarioTemplate bloco = BlocoHorarioTemplate.builder()
                .id(UUID.randomUUID())
                .templateSemanal(template)
                .diaSemana(DiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(8, 0))
                .horaFim(LocalTime.of(9, 30))
                .build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(templateSemanalFinder.findById(templateId)).thenReturn(template);
        when(blocoHorarioTemplateFinder.findAllByTemplateSemanal(template)).thenReturn(List.of(bloco));

        GradeSemanalResponse grade = templateSemanalService.visualizarGradeSemanal(userId, templateId);

        assertThat(grade.templateId()).isEqualTo(templateId);
        assertThat(grade.grade().get(DiaSemana.SEGUNDA)).hasSize(1);
    }

    @Test
    @DisplayName("Deve editar template semanal com sucesso")
    void deveEditarTemplate() {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        TemplateSemanal template = TemplateSemanal.builder().id(templateId).nome("Nome Antigo").ativo(true).build();
        EditarTemplateSemanalRequest request = new EditarTemplateSemanalRequest("Novo Nome", false);

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(templateSemanalFinder.findById(templateId)).thenReturn(template);

        TemplateSemanalResponse response = templateSemanalService.editarTemplate(userId, templateId, request);

        assertThat(response.nome()).isEqualTo("Novo Nome");
        assertThat(response.ativo()).isFalse();
        verify(templateSemanalRepository).save(template);
    }

    @Test
    @DisplayName("Deve apagar template semanal com sucesso")
    void deveApagarTemplate() {
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(userId).build();
        TemplateSemanal template = TemplateSemanal.builder().id(templateId).build();

        when(userFinder.findById(userId)).thenReturn(usuario);
        when(templateSemanalFinder.findById(templateId)).thenReturn(template);

        templateSemanalService.apagarTemplate(userId, templateId);

        verify(templateSemanalRepository).delete(template);
    }
}
