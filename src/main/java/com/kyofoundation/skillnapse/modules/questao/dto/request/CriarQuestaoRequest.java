package com.kyofoundation.skillnapse.modules.questao.dto.request;

import com.kyofoundation.skillnapse.modules.questao.enums.DificuldadeQuestao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CriarQuestaoRequest(
        @NotBlank(message = "O assunto geral é obrigatório.")
        @Size(max = 100, message = "O assunto geral deve ter no máximo 100 caracteres.")
        String assuntoGeral,

        @NotBlank(message = "O tópico de referência é obrigatório.")
        @Size(max = 150, message = "O tópico de referência deve ter no máximo 150 caracteres.")
        String topicoReferencia,

        @NotBlank(message = "O enunciado da questão é obrigatório.")
        String enunciado,

        @NotBlank(message = "A explicação do gabarito é obrigatória.")
        String explicacaoGabarito,

        @NotNull(message = "A dificuldade da questão é obrigatória.")
        DificuldadeQuestao dificuldade,

        @Size(max = 50, message = "A banca examinadora deve ter no máximo 50 caracteres.")
        String banca,

        @Min(value = 1900, message = "O ano deve ser maior ou igual a 1900.")
        @Max(value = 2100, message = "O ano deve ser menor ou igual a 2100.")
        Integer ano,

        @NotEmpty(message = "A lista de alternativas não pode ser vazia.")
        @Valid
        List<CriarAlternativaRequest> alternativas
) {
}
