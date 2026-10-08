package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AtualizarTopicosRevisaoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoDiarioRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.CriarBlocoRevisaoTemplateRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.BlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ConteudoBlocoRevisaoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.BlocoRevisaoService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cronogramas/blocos-revisao")
@RequiredArgsConstructor
@Tag(name = "Blocos de Revisão", description = "Endpoints para criação e execução de blocos especializados de revisão vinculados a tópicos específicos")
@SecurityRequirement(name = "bearerAuth")
public class BlocoRevisaoController {

    private final BlocoRevisaoService blocoRevisaoService;

    @PostMapping("/template")
    @Operation(summary = "Criar bloco de revisão no template", description = "Cria um bloco semanal dedicado do tipo REVISAO associado a uma lista explícita de tópicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bloco de revisão criado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, horários inconsistentes ou sobreposição",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Template, matéria ou tópicos não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template semanal não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> criarBlocoRevisaoTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CriarBlocoRevisaoTemplateRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(blocoRevisaoService.criarBlocoRevisaoTemplate(userId, request));
    }

    @PostMapping("/diario")
    @Operation(summary = "Criar bloco de revisão avulso diário", description = "Cria um bloco pontual na agenda diária do tipo REVISAO associado a uma lista explícita de tópicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bloco diário de revisão criado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horários inconsistentes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Matéria ou tópicos não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> criarBlocoRevisaoDiario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CriarBlocoRevisaoDiarioRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(blocoRevisaoService.criarBlocoRevisaoDiario(userId, request));
    }

    @PutMapping("/template/{blocoId}/topicos")
    @Operation(summary = "Atualizar tópicos do bloco template", description = "Redefine a lista de tópicos vinculados a um bloco de revisão existente na grade semanal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópicos do bloco atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Lista vazia ou bloco não é do tipo REVISAO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Bloco ou tópicos não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> atualizarTopicosBlocoTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID blocoId,
            @Valid @RequestBody AtualizarTopicosRevisaoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.atualizarTopicosBlocoTemplate(userId, blocoId, request));
    }

    @PutMapping("/diario/{excecaoId}/topicos")
    @Operation(summary = "Atualizar tópicos do bloco diário", description = "Redefine a lista de tópicos vinculados a uma exceção diária de revisão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tópicos da exceção atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Lista vazia ou exceção não é do tipo REVISAO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Exceção ou tópicos não pertencem ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exceção não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> atualizarTopicosBlocoDiario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID excecaoId,
            @Valid @RequestBody AtualizarTopicosRevisaoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.atualizarTopicosBlocoDiario(userId, excecaoId, request));
    }

    @GetMapping("/template/{blocoId}")
    @Operation(summary = "Visualizar bloco de revisão template", description = "Retorna os detalhes de um bloco de revisão cadastrado no template semanal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloco de revisão recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Bloco não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> visualizarBlocoRevisaoTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID blocoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.visualizarBlocoRevisaoTemplate(userId, blocoId));
    }

    @GetMapping("/diario/{excecaoId}")
    @Operation(summary = "Visualizar bloco de revisão diário", description = "Retorna os detalhes de um bloco de revisão avulso na agenda diária.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bloco de revisão diário recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = BlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Exceção não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exceção não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlocoRevisaoResponse> visualizarBlocoRevisaoDiario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID excecaoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.visualizarBlocoRevisaoDiario(userId, excecaoId));
    }

    @GetMapping("/template/{blocoId}/conteudo")
    @Operation(summary = "Obter conteúdo a revisar do bloco template",
            description = "Retorna a listagem estrita de flashcards para revisão pertencentes aos tópicos associados a este bloco na data de referência informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdo de revisão retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = ConteudoBlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bloco não é do tipo REVISAO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Bloco não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bloco não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConteudoBlocoRevisaoResponse> obterConteudoRevisaoTemplate(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID blocoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.obterConteudoRevisaoTemplate(userId, blocoId, dataReferencia));
    }

    @GetMapping("/diario/{excecaoId}/conteudo")
    @Operation(summary = "Obter conteúdo a revisar da exceção diária",
            description = "Retorna a listagem estrita de flashcards para revisão pertencentes aos tópicos associados a esta exceção diária.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteúdo de revisão retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = ConteudoBlocoRevisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Exceção não é do tipo REVISAO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Exceção não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exceção não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ConteudoBlocoRevisaoResponse> obterConteudoRevisaoDiario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID excecaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(blocoRevisaoService.obterConteudoRevisaoDiario(userId, excecaoId, dataReferencia));
    }
}
