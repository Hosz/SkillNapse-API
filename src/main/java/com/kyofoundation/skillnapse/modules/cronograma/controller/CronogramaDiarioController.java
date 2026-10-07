package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.EditarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.RegistrarExcecaoDiariaRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.AgendaDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ExcecaoDiariaResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.CronogramaDiarioService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cronogramas/diario")
@RequiredArgsConstructor
@Tag(name = "Cronograma Diário (Overrides)", description = "Endpoints para personalização e projeção diária do cronograma")
@SecurityRequirement(name = "bearerAuth")
public class CronogramaDiarioController {

    private final CronogramaDiarioService cronogramaDiarioService;

    @PostMapping("/{data}/excecoes")
    @Operation(summary = "Registrar exceção diária", description = "Registra uma alteração pontual (cancelamento, substituição de horário ou bloco avulso) para uma data específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exceção diária registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExcecaoDiariaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou conflito de dias/horários",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Recurso indicado não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Exceção já registrada para este bloco nesta data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExcecaoDiariaResponse> registrarExcecao(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Valid @RequestBody RegistrarExcecaoDiariaRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(cronogramaDiarioService.registrarExcecao(userId, data, request));
    }

    @PatchMapping("/excecoes/{excecaoId}")
    @Operation(summary = "Editar exceção diária", description = "Atualiza os parâmetros de uma exceção previamente cadastrada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exceção diária atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ExcecaoDiariaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário inconsistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido à exceção de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exceção diária não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExcecaoDiariaResponse> editarExcecaoDiaria(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID excecaoId,
            @Valid @RequestBody EditarExcecaoDiariaRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cronogramaDiarioService.editarExcecaoDiaria(userId, excecaoId, request));
    }

    @DeleteMapping("/excecoes/{excecaoId}")
    @Operation(summary = "Remover exceção pontual", description = "Remove uma única exceção pontual, restaurando o bloco original do template.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exceção removida com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido à exceção de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exceção diária não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> removerExcecaoDiaria(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID excecaoId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        cronogramaDiarioService.removerExcecaoDiaria(userId, excecaoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{data}/resetar")
    @Operation(summary = "Resetar agenda do dia", description = "Remove todas as exceções da data informada, fazendo a rotina retornar 100% ao template semanal.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agenda do dia resetada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> resetarAgendaDia(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        UUID userId = UUID.fromString(jwt.getSubject());
        cronogramaDiarioService.resetarAgendaDia(userId, data);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{data}")
    @Operation(summary = "Visualizar agenda do dia", description = "Retorna a projeção final da agenda do dia mesclando o template semanal com os overrides cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agenda do dia projetada com sucesso",
                    content = @Content(schema = @Schema(implementation = AgendaDiariaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AgendaDiariaResponse> visualizarAgendaDia(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(cronogramaDiarioService.visualizarAgendaDia(userId, data));
    }
}
