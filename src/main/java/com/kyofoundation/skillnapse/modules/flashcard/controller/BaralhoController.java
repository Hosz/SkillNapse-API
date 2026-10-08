package com.kyofoundation.skillnapse.modules.flashcard.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.AtualizarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.request.CriarBaralhoRequest;
import com.kyofoundation.skillnapse.modules.flashcard.dto.response.BaralhoResponse;
import com.kyofoundation.skillnapse.modules.flashcard.service.BaralhoService;
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
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/baralhos")
@RequiredArgsConstructor
@Tag(name = "Baralhos de Flashcards", description = "Endpoints para gerenciamento de baralhos e agrupamento de cards para estudo")
@SecurityRequirement(name = "bearerAuth")
public class BaralhoController {

    private final BaralhoService baralhoService;

    @PostMapping
    @Operation(summary = "Criar baralho", description = "Cadastra um novo baralho de flashcards para o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Baralho criado com sucesso",
                    content = @Content(schema = @Schema(implementation = BaralhoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do baralho inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Matéria associada pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Matéria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BaralhoResponse> criarBaralho(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CriarBaralhoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(baralhoService.criarBaralho(userId, request));
    }

    @GetMapping
    @Operation(summary = "Listar baralhos", description = "Lista os baralhos do usuário autenticado de forma paginada com totalizadores de cards e revisões pendentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de baralhos recuperada com sucesso")
    })
    public ResponseEntity<Page<BaralhoResponse>> listarBaralhos(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @ParameterObject Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(baralhoService.listarBaralhos(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter baralho por ID", description = "Recupera as informações detalhadas de um baralho específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Baralho recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = BaralhoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Baralho pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Baralho não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BaralhoResponse> obterBaralho(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do baralho") @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(baralhoService.obterBaralho(userId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar baralho", description = "Atualiza os dados cadastrais de um baralho existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Baralho atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BaralhoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Baralho ou matéria pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Baralho ou matéria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BaralhoResponse> atualizarBaralho(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do baralho") @PathVariable UUID id,
            @Valid @RequestBody AtualizarBaralhoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(baralhoService.atualizarBaralho(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Apagar baralho", description = "Remove um baralho e todos os seus flashcards vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Baralho removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Baralho pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Baralho não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> apagarBaralho(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID do baralho") @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        baralhoService.apagarBaralho(userId, id);
        return ResponseEntity.noContent().build();
    }
}
