package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarTemplateSemanalRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.GradeSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.TemplateSemanalResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.TemplateSemanalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cronogramas/templates")
@RequiredArgsConstructor
@Tag(name = "Templates Semanais", description = "Endpoints para gerenciamento do template de rotina semanal base")
@SecurityRequirement(name = "bearerAuth")
public class TemplateSemanalController {

    private final TemplateSemanalService templateSemanalService;

    @PostMapping
    @Operation(summary = "Criar template semanal", description = "Cria um novo template de rotina semanal para o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template semanal criado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateSemanalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado / Token JWT ausente ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TemplateSemanalResponse> criarTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CriarTemplateSemanalRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(templateSemanalService.criarTemplate(userId, request));
    }

    @GetMapping
    @Operation(summary = "Listar templates semanais", description = "Recupera a listagem paginada de todos os templates semanais do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates recuperados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TemplateSemanalResponse>> listarTemplates(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.listarTemplates(userId, pageable));
    }

    @GetMapping("/ativo")
    @Operation(summary = "Visualizar template ativo", description = "Recupera o template semanal atualmente em vigor na rotina do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template ativo recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateSemanalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum template ativo encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TemplateSemanalResponse> visualizarTemplateAtivo(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.visualizarTemplateAtivo(userId));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Visualizar template semanal", description = "Recupera os detalhes de um template semanal específico por identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template semanal recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateSemanalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template semanal não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TemplateSemanalResponse> visualizarTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.visualizarTemplate(userId, templateId));
    }

    @GetMapping("/{templateId}/grade")
    @Operation(summary = "Visualizar grade semanal completa", description = "Recupera a grade da semana com blocos agrupados por dia e ordenados por horário de início.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade semanal recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = GradeSemanalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template semanal não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GradeSemanalResponse> visualizarGradeSemanal(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.visualizarGradeSemanal(userId, templateId));
    }

    @PatchMapping("/{templateId}")
    @Operation(summary = "Editar template semanal", description = "Atualiza nome e/ou status de ativação de um template existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template semanal atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateSemanalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de edição inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template semanal não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TemplateSemanalResponse> editarTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            @Valid @RequestBody EditarTemplateSemanalRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.editarTemplate(userId, templateId, request));
    }

    @PatchMapping("/{templateId}/ativar")
    @Operation(summary = "Ativar template semanal", description = "Ativa o template semanal indicado e desativa automaticamente os demais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template ativado com sucesso",
                    content = @Content(schema = @Schema(implementation = TemplateSemanalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TemplateSemanalResponse> ativarTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(templateSemanalService.ativarTemplate(userId, templateId));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Apagar template semanal", description = "Remove o template semanal e todos os seus blocos de horário vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template semanal removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        templateSemanalService.apagarTemplate(userId, templateId);
        return ResponseEntity.noContent().build();
    }
}
