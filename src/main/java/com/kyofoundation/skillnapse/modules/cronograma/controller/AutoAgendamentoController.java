package com.kyofoundation.skillnapse.modules.cronograma.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.AplicarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.request.GerarAutoAgendamentoRequest;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.ResultadoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.dto.response.SugestaoAutoAgendamentoResponse;
import com.kyofoundation.skillnapse.modules.cronograma.service.AutoAgendamentoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cronogramas/auto-agendamento")
@RequiredArgsConstructor
@Tag(name = "Auto-Agendamento Inteligente (Cronograma IA)", description = "Distribuição inteligente de matérias e tópicos em templates semanais com base em disponibilidade e pedagogia via Spring AI")
@SecurityRequirement(name = "bearerAuth")
public class AutoAgendamentoController {

    private final AutoAgendamentoService autoAgendamentoService;

    @PostMapping("/sugestao")
    @Operation(summary = "Simular cronograma inteligente", description = "Calcula a grade semanal pedagógica com IA baseada na disponibilidade do aluno, sem salvar no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão de cronograma gerada com sucesso",
                    content = @Content(schema = @Schema(implementation = SugestaoAutoAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Janelas horárias inválidas ou plano sem matérias",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Plano de estudo não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SugestaoAutoAgendamentoResponse> gerarSugestao(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GerarAutoAgendamentoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(autoAgendamentoService.gerarSugestao(userId, request));
    }

    @PostMapping("/aplicar")
    @Operation(summary = "Gerar e aplicar cronograma no template", description = "Gera a grade inteligente com IA e persiste os blocos no template semanal (criando um novo template ou atualizando existente).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cronograma inteligente aplicado e persistido com sucesso",
                    content = @Content(schema = @Schema(implementation = ResultadoAutoAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inconsistentes ou horários conflitantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Plano ou template não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo ou template não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultadoAutoAgendamentoResponse> aplicarAgendamento(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AplicarAutoAgendamentoRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(autoAgendamentoService.aplicarAgendamento(userId, request));
    }
}
