package com.kyofoundation.skillnapse.modules.ai.controller;

import com.kyofoundation.skillnapse.modules.ai.dto.AiGenerationResponse;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import com.kyofoundation.skillnapse.modules.ai.service.AiOrchestratorService;
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
public class AiController {

    private final AiOrchestratorService aiOrchestratorService;

    @PostMapping("/generate")
    public ResponseEntity<AiGenerationResponse> generate(@RequestBody @Valid PromptRequest request) {
        AiGenerationResponse response = aiOrchestratorService.generate(request);
        return ResponseEntity.ok(response);
    }
}
