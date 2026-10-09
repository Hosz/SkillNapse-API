package com.kyofoundation.skillnapse.modules.redacao.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.modules.redacao.dto.payload.FeedbackCorrecaoIaPayload;
import org.springframework.stereotype.Component;

@Component
public class JsonFeedbackRedacaoSupport {

    private final ObjectMapper objectMapper;

    public JsonFeedbackRedacaoSupport() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public JsonFeedbackRedacaoSupport(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public String serializar(FeedbackCorrecaoIaPayload feedback) {
        if (feedback == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(feedback);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Falha ao converter feedback da IA para JSON: " + e.getMessage());
        }
    }

    public FeedbackCorrecaoIaPayload desserializar(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, FeedbackCorrecaoIaPayload.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Falha ao desserializar feedback da IA a partir do JSON: " + e.getMessage());
        }
    }
}
