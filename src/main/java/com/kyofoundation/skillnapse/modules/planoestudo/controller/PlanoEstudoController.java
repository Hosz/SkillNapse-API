package com.kyofoundation.skillnapse.modules.planoestudo.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.EdicaoPlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.request.PlanoEstudoRequest;
import com.kyofoundation.skillnapse.modules.planoestudo.dto.response.PlanoEstudoResponse;
import com.kyofoundation.skillnapse.modules.planoestudo.service.PlanoEstudoService;
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
@RequestMapping("/api/v1/plano-estudo")
@RequiredArgsConstructor
@Tag(name = "Planos de Estudo", description = "Endpoints para gerenciamento do plano de estudo raiz do estudante")
@SecurityRequirement(name = "bearerAuth")
public class PlanoEstudoController {

    private final PlanoEstudoService planoEstudoService;

    @PostMapping
    @Operation(summary = "Criar plano de estudo", description = "Cria um novo plano de estudo vinculado ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plano de estudo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PlanoEstudoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado / Token JWT ausente ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlanoEstudoResponse> criarPlano(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PlanoEstudoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(planoEstudoService.criarPlano(request, userId));
    }

    @GetMapping("/{planoEstudoId}")
    @Operation(summary = "Visualizar plano de estudo", description = "Recupera os detalhes de um plano de estudo específico pertencente ao usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano de estudo recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = PlanoEstudoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido: plano não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlanoEstudoResponse> visualizarPlano(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID planoEstudoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(planoEstudoService.visualizarPlano(userId, planoEstudoId));
    }

    @PatchMapping("/{planoEstudoId}")
    @Operation(summary = "Editar plano de estudo", description = "Atualiza parcialmente título, descrição ou status ativo do plano de estudo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano de estudo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PlanoEstudoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de edição inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido: plano não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlanoEstudoResponse> editarPlano(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID planoEstudoId,
            @Valid @RequestBody EdicaoPlanoEstudoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(planoEstudoService.editarPlano(userId, planoEstudoId, request));
    }

    @DeleteMapping("/{planoEstudoId}")
    @Operation(summary = "Apagar plano de estudo", description = "Remove o plano de estudos e todas as suas matérias e tópicos em cascata.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plano de estudo apagado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido: plano não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarPlano(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID planoEstudoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        planoEstudoService.apagarPlano(userId, planoEstudoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar planos de estudo", description = "Lista paginada de todos os planos de estudo cadastrados pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de planos recuperada com sucesso")
    })
    public ResponseEntity<Page<PlanoEstudoResponse>> listarPlanos(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(planoEstudoService.listarPlanos(userId, pageable));
    }
}
