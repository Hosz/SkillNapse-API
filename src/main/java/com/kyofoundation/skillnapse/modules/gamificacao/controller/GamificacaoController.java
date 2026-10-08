package com.kyofoundation.skillnapse.modules.gamificacao.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.request.AtualizarMetaDiariaRequest;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.MetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.PainelGamificacaoResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.ProgressoMetaDiariaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.dto.response.StatusOfensivaResponse;
import com.kyofoundation.skillnapse.modules.gamificacao.service.MetaDiariaService;
import com.kyofoundation.skillnapse.modules.gamificacao.service.OfensivaService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamificacao")
@RequiredArgsConstructor
@Tag(name = "Gamificação e Hábitos", description = "Endpoints para acompanhamento de ofensivas (streaks) e metas diárias de estudo")
@SecurityRequirement(name = "bearerAuth")
public class GamificacaoController {

    private final OfensivaService ofensivaService;
    private final MetaDiariaService metaDiariaService;

    @GetMapping("/painel")
    @Operation(summary = "Painel completo de gamificação", description = "Retorna o status consolidado da ofensiva (streaks) e o progresso das metas diárias de horas e questões.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel consolidado retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = PainelGamificacaoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PainelGamificacaoResponse> obterPainel(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(metaDiariaService.obterPainelCompleto(userId, dataReferencia));
    }

    @GetMapping("/ofensiva")
    @Operation(summary = "Consultar status da ofensiva", description = "Retorna a sequência atual de dias consecutivos, recorde histórico e se o usuário já estudou hoje.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da ofensiva retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = StatusOfensivaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StatusOfensivaResponse> obterOfensiva(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ofensivaService.obterStatusOfensiva(userId));
    }

    @PostMapping("/ofensiva/registrar-estudo")
    @Operation(summary = "Registrar estudo na ofensiva", description = "Registra um evento de estudo e avança a ofensiva (streak) de dias consecutivos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ofensiva atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = StatusOfensivaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StatusOfensivaResponse> registrarEstudoOfensiva(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEstudo) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ofensivaService.registrarEstudo(userId, dataEstudo));
    }

    @GetMapping("/metas")
    @Operation(summary = "Consultar metas diárias configuradas", description = "Retorna os objetivos diários de estudo líquido e quantidade de questões resolvidas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metas diárias configuradas retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = MetaDiariaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MetaDiariaResponse> obterConfiguracaoMetas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(metaDiariaService.obterConfiguracaoMetas(userId));
    }

    @PutMapping("/metas")
    @Operation(summary = "Atualizar metas diárias", description = "Atualiza os objetivos de tempo de estudo em minutos e quantidade de questões diárias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metas diárias atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = MetaDiariaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Valores de metas inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MetaDiariaResponse> atualizarMetas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AtualizarMetaDiariaRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(metaDiariaService.atualizarMetas(userId, request));
    }

    @GetMapping("/metas/progresso")
    @Operation(summary = "Consultar progresso diário das metas", description = "Retorna a porcentagem atingida e os valores realizados no dia para horas estudadas e questões resolvidas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso das metas retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProgressoMetaDiariaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProgressoMetaDiariaResponse> obterProgressoDiario(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(metaDiariaService.obterProgressoDiario(userId, dataReferencia));
    }
}
