package com.kyofoundation.skillnapse.modules.ai.controller;

import com.kyofoundation.skillnapse.common.exception.ErrorResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "Inteligência Artificial", description = "Endpoints para orquestração desacoplada de IA com LLMs (BYOK)")
public class AiController {

    private final AiOrchestratorService aiOrchestratorService;

    @PostMapping("/generate")
    @Operation(
            summary = "Gera resposta via IA",
            description = "Recebe um prompt com parâmetros opcionais (temperatura, mensagem de sistema) e retorna a resposta processada pelo provedor de LLM ativo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resposta gerada com sucesso pelo LLM",
                    content = @Content(schema = @Schema(implementation = AiGenerationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (prompt vazio ou temperatura fora do range)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AiGenerationResponse> generate(@RequestBody @Valid PromptRequest request) {
        AiGenerationResponse response = aiOrchestratorService.generate(request);
        return ResponseEntity.ok(response);
    }
}
