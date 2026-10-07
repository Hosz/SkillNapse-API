package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarBlocoHorarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoHorarioResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.BlocoHorarioTemplateService;
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
@RequestMapping("/api/v1/cronogramas/templates/{templateId}/blocos")
@RequiredArgsConstructor
@Tag(name = "Blocos de Horário do Template", description = "Endpoints para gerenciamento dos blocos granulares de tempo na grade semanal")
@SecurityRequirement(name = "bearerAuth")
public class BlocoHorarioTemplateController {

    private final BlocoHorarioTemplateService blocoHorarioTemplateService;

    @PostMapping
    @Operation(summary = "Adicionar bloco de horário", description = "Adiciona um novo bloco granular de tempo ao template semanal informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bloco de horário adicionado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoHorarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, horários inconsistentes ou sobreposição detectada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template, matéria ou tópico não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template semanal não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoHorarioResponse> adicionarBlocoHorario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            @Valid @RequestBody CriarBlocoHorarioRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(blocoHorarioTemplateService.adicionarBlocoHorario(userId, templateId, request));
    }

    @GetMapping
    @Operation(summary = "Listar blocos de horário do template", description = "Recupera a listagem paginada dos blocos de horário vinculados ao template semanal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blocos de horário recuperados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<BlocoHorarioResponse>> listarBlocosHorario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoHorarioTemplateService.listarBlocosHorario(userId, templateId, pageable));
    }

    @GetMapping("/{blocoId}")
    @Operation(summary = "Visualizar bloco de horário", description = "Recupera os detalhes de um bloco de horário específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloco de horário recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoHorarioResponse.class))),
            @ApiResponse(responseCode = "403", description = "Bloco não pertence ao template ou usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco ou template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoHorarioResponse> visualizarBlocoHorario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            @PathVariable UUID blocoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoHorarioTemplateService.visualizarBlocoHorario(userId, templateId, blocoId));
    }

    @PatchMapping("/{blocoId}")
    @Operation(summary = "Editar bloco de horário", description = "Atualiza dia, intervalo de horários, tipo ou matérias vinculadas do bloco.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloco de horário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoHorarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Intervalo inválido ou conflito de horário com outro bloco",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido a recursos que não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco ou template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoHorarioResponse> editarBlocoHorario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            @PathVariable UUID blocoId,
            @Valid @RequestBody EditarBlocoHorarioRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoHorarioTemplateService.editarBlocoHorario(userId, templateId, blocoId, request));
    }

    @DeleteMapping("/{blocoId}")
    @Operation(summary = "Remover bloco de horário", description = "Exclui um bloco de horário específico da grade do template semanal.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bloco de horário excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Bloco não pertence ao template ou usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco ou template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarBlocoHorario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID templateId,
            @PathVariable UUID blocoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        blocoHorarioTemplateService.apagarBlocoHorario(userId, templateId, blocoId);
        return ResponseEntity.noContent().build();
    }
}
