package com.kyofoundation.skillnapse.modules.canvas.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.canvas.dto.request.SalvarRascunhoCanvasRequest;
import com.kyofoundation.skillnapse.modules.canvas.dto.response.RascunhoCanvasResponse;
import com.kyofoundation.skillnapse.modules.canvas.service.RascunhoCanvasService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/canvas")
@RequiredArgsConstructor
@Tag(name = "Canvas e Rascunho Digital", description = "Endpoints para gerenciamento de rascunhos livres e anotações vetoriais acopladas a tópicos")
@SecurityRequirement(name = "bearerAuth")
public class RascunhoCanvasController {

    private final RascunhoCanvasService rascunhoCanvasService;

    @PutMapping("/topico/{topicoId}")
    @Operation(summary = "Salvar ou atualizar rascunho de canvas por tópico", description = "Grava ou atualiza de forma idempotente (upsert) o desenho vetorial associado ao tópico informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho de canvas salvo/atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = RascunhoCanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado ou payload excede o limite",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tópico pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RascunhoCanvasResponse> salvarRascunhoPorTopico(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do tópico acoplado") @PathVariable UUID topicoId,
            @Valid @RequestBody SalvarRascunhoCanvasRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(rascunhoCanvasService.salvarRascunho(userId, topicoId, request));
    }

    @PostMapping
    @Operation(summary = "Salvar ou atualizar rascunho de canvas", description = "Grava ou atualiza de forma idempotente (upsert) o desenho vetorial informando o tópico no corpo da requisição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho de canvas salvo/atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = RascunhoCanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado ou ID de tópico ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tópico pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RascunhoCanvasResponse> salvarRascunho(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SalvarRascunhoCanvasRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(rascunhoCanvasService.salvarRascunho(userId, request.topicoId(), request));
    }

    @GetMapping("/topico/{topicoId}")
    @Operation(summary = "Obter rascunho de canvas por tópico", description = "Recupera o rascunho de canvas associado ao tópico informado para o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho de canvas recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = RascunhoCanvasResponse.class))),
            @ApiResponse(responseCode = "403", description = "Tópico informado pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rascunho de canvas ou tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RascunhoCanvasResponse> obterRascunhoPorTopico(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do tópico acoplado") @PathVariable UUID topicoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(rascunhoCanvasService.obterRascunhoPorTopico(userId, topicoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter rascunho de canvas por ID", description = "Recupera um rascunho de canvas diretamente por seu identificador único.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho de canvas recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = RascunhoCanvasResponse.class))),
            @ApiResponse(responseCode = "403", description = "Rascunho pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rascunho de canvas não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RascunhoCanvasResponse> obterRascunhoPorId(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do rascunho de canvas") @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(rascunhoCanvasService.obterRascunhoPorId(userId, id));
    }

    @GetMapping
    @Operation(summary = "Listar rascunhos de canvas do usuário", description = "Retorna todos os rascunhos de canvas do usuário autenticado de forma paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada recuperada com sucesso")
    })
    public ResponseEntity<Page<RascunhoCanvasResponse>> listarRascunhos(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @ParameterObject Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(rascunhoCanvasService.listarRascunhos(userId, pageable));
    }

    @DeleteMapping("/topico/{topicoId}")
    @Operation(summary = "Apagar rascunho de canvas por tópico", description = "Remove as anotações e desenhos do tópico informado para o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rascunho de canvas removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Tópico informado pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rascunho de canvas ou tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarRascunhoPorTopico(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do tópico acoplado") @PathVariable UUID topicoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        rascunhoCanvasService.apagarRascunhoPorTopico(userId, topicoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar rascunho de canvas por ID", description = "Remove um rascunho de canvas diretamente por seu identificador único.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rascunho de canvas removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Rascunho pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rascunho de canvas não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarRascunhoPorId(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do rascunho de canvas") @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        rascunhoCanvasService.apagarRascunhoPorId(userId, id);
        return ResponseEntity.noContent().build();
    }
}
