package com.kyofoundation.skillnapse.modules.questao.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.questao.dto.request.GerarSimuladoAdaptativoRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.response.SimuladoAdaptativoResponse;
import com.kyofoundation.skillnapse.modules.questao.service.SimuladoAdaptativoService;
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
@RequestMapping("/api/v1/simulados/adaptativo")
@RequiredArgsConstructor
@Tag(name = "Simulados Adaptativos", description = "Endpoints para geração de simulados adaptativos sob demanda via Inteligência Artificial")
@SecurityRequirement(name = "bearerAuth")
public class SimuladoAdaptativoController {

    private final SimuladoAdaptativoService simuladoAdaptativoService;

    @PostMapping("/gerar")
    @Operation(summary = "Gerar simulado adaptativo por IA", description = "Gera um simulado personalizado utilizando IA com base em fraquezas conceituais ou tópicos especificados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Simulado adaptativo gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = SimuladoAdaptativoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou plano de estudos sem tópicos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao plano ou tópico",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário, plano de estudo ou tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SimuladoAdaptativoResponse> gerar(
            @Valid @RequestBody GerarSimuladoAdaptativoRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(simuladoAdaptativoService.gerarSimuladoAdaptativo(usuarioId, request));
    }
}
