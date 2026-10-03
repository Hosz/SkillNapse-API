package com.kyofoundation.skillnapse.modules.ai.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.ai.dto.PromptRequest;
import org.springframework.stereotype.Component;

@Component
public class AiPromptValidator {

    public void validatePromptRequest(PromptRequest request) {
        if (request == null) {
            throw new BadRequestException("A requisição de prompt não pode ser nula.");
        }
        if (request.prompt() == null || request.prompt().trim().isEmpty()) {
            throw new BadRequestException("O prompt não pode ser nulo ou vazio.");
        }
        if (request.temperature() != null && (request.temperature() < 0.0 || request.temperature() > 2.0)) {
            throw new BadRequestException("A temperatura deve estar entre 0.0 e 2.0.");
        }
        if (request.maxTokens() != null && request.maxTokens() <= 0) {
            throw new BadRequestException("A quantidade máxima de tokens deve ser maior que zero.");
        }
    }

    public <T> void validateStructuredRequest(PromptRequest request, Class<T> responseType) {
        validatePromptRequest(request);
        if (responseType == null) {
            throw new BadRequestException("O tipo de classe de resposta (responseType) não pode ser nulo.");
        }
    }
}
