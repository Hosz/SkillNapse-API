package com.kyofoundation.skillnapse.modules.desempenho.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.PainelGeralDesempenhoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.RelatorioLacunasResponse;
import com.kyofoundation.skillnapse.modules.desempenho.dto.response.TopicoCriticoResponse;
import com.kyofoundation.skillnapse.modules.desempenho.service.DesempenhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/desempenho")
@RequiredArgsConstructor
@Tag(name = "Desempenho e Mapeamento de Lacunas", description = "Endpoints analíticos para diagnóstico de proficiência, cálculo de prontidão e mapeamento de fraquezas conceituais")
public class DesempenhoController {

    private final DesempenhoService desempenhoService;

    @GetMapping("/planos/{planoId}/lacunas")
    @Operation(summary = "Obter relatório analítico de lacunas do plano de estudo",
            description = "Cruza a taxa de acerto do estudante com o peso do edital e proficiência, diagnosticando a criticidade de cada tópico e calculando o índice global de prontidão do plano.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório analítico gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = RelatorioLacunasResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT não fornecido ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Plano não pertence ao usuário ou usuário inativo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RelatorioLacunasResponse> obterRelatorioLacunas(
            @PathVariable UUID planoId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(desempenhoService.obterRelatorioLacunas(userId, planoId));
    }

    @GetMapping("/planos/{planoId}/topicos-criticos")
    @Operation(summary = "Obter ranking dos tópicos mais críticos do plano de estudo",
            description = "Retorna os tópicos com maior índice de severidade de lacuna (peso alto e baixo rendimento) para direcionar revisões e simulados adaptativos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking de tópicos críticos retornado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopicoCriticoResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Parâmetro limite inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT não fornecido ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Plano não pertence ao usuário ou usuário inativo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plano de estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TopicoCriticoResponse>> obterTopicosCriticos(
            @PathVariable UUID planoId,
            @RequestParam(defaultValue = "5") int limite,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(desempenhoService.obterTopicosCriticos(userId, planoId, limite));
    }

    @GetMapping("/geral")
    @Operation(summary = "Obter painel geral consolidado de desempenho do estudante",
            description = "Consolida as métricas de aproveitamento de todas as matérias cadastradas nos planos do estudante e destaca as principais lacunas globais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel consolidado retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = PainelGeralDesempenhoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT não fornecido ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário inativo ou não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PainelGeralDesempenhoResponse> obterPainelGeral(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(desempenhoService.obterPainelGeral(userId));
    }
}
