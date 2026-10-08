package com.kyofoundation.skillnapse.modules.cronograma.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Requisição para aplicação e persistência dos blocos sugeridos em um template semanal")
public record AplicarAutoAgendamentoRequest(
        @NotNull(message = "O ID do plano de estudo é obrigatório.")
        @Schema(description = "Identificador único do plano de estudo")
        UUID planoEstudoId,

        @Schema(description = "Identificador de um template semanal existente a ser atualizado (se nulo, um novo template será criado)")
        UUID templateSemanalId,

        @Size(max = 100, message = "O nome do template não pode ultrapassar 100 caracteres.")
        @Schema(description = "Nome para o novo template (caso templateSemanalId seja nulo)", example = "Cronograma IA 2026")
        String nomeTemplate,

        @Schema(description = "Indica se blocos existentes no template devem ser removidos antes da inserção dos novos blocos", defaultValue = "true")
        Boolean limparBlocosExistentes,

        @Schema(description = "Resumo pedagógico gerado pela IA na simulação")
        String resumoPedagogico,

        @NotEmpty(message = "Informe ao menos um bloco para aplicação no template.")
        @Schema(description = "Lista de blocos de estudo aprovados pelo estudante para gravação")
        List<@Valid BlocoParaAplicarRequest> blocos
) {
    public boolean limparBlocosExistentesEfetivo() {
        return limparBlocosExistentes == null || limparBlocosExistentes;
    }
}
