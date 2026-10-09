package com.kyofoundation.skillnapse.modules.questao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.questao.entity.AlternativaQuestao;
import com.kyofoundation.skillnapse.modules.questao.entity.Questao;
import com.kyofoundation.skillnapse.modules.questao.entity.Simulado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResolucaoQuestaoValidatorTest {

    private ResolucaoQuestaoValidator validator;
    private Usuario usuario;
    private Questao questao;
    private AlternativaQuestao alternativa;

    @BeforeEach
    void setUp() {
        validator = new ResolucaoQuestaoValidator();
        usuario = Usuario.builder().id(UUID.randomUUID()).email("aluno@skillnapse.com").build();
        questao = Questao.builder().id(UUID.randomUUID()).enunciado("Enunciado").build();
        alternativa = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .questao(questao)
                .letra("A")
                .correta(true)
                .build();
    }

    @Test
    @DisplayName("Deve validar resolução com sucesso")
    void deveValidarResolucaoComSucesso() {
        assertThatCode(() -> validator.validarResolucao(questao, alternativa, 45, null, null, usuario))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException para questão nula")
    void deveLancarBadRequestParaQuestaoNula() {
        assertThatThrownBy(() -> validator.validarResolucao(null, alternativa, 45, null, null, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A questão a ser respondida não pode ser nula.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando alternativa não pertence à questão")
    void deveLancarBadRequestQuandoAlternativaPertenceAOutraQuestao() {
        Questao outraQuestao = Questao.builder().id(UUID.randomUUID()).build();
        AlternativaQuestao outraAlt = AlternativaQuestao.builder()
                .id(UUID.randomUUID())
                .questao(outraQuestao)
                .build();

        assertThatThrownBy(() -> validator.validarResolucao(questao, outraAlt, 45, null, null, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A alternativa informada não pertence à questão indicada.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando tempo gasto é negativo")
    void deveLancarBadRequestQuandoTempoNegativo() {
        assertThatThrownBy(() -> validator.validarResolucao(questao, alternativa, -10, null, null, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O tempo gasto deve ser maior ou igual a zero.");
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando simulado pertence a outro usuário")
    void deveLancarForbiddenQuandoSimuladoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        Simulado simulado = Simulado.builder().id(UUID.randomUUID()).usuario(outroUsuario).concluido(false).build();

        assertThatThrownBy(() -> validator.validarResolucao(questao, alternativa, 30, simulado, null, usuario))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para responder questões neste simulado.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando simulado já está concluído")
    void deveLancarBadRequestQuandoSimuladoJaConcluido() {
        Simulado simulado = Simulado.builder().id(UUID.randomUUID()).usuario(usuario).concluido(true).build();

        assertThatThrownBy(() -> validator.validarResolucao(questao, alternativa, 30, simulado, null, usuario))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível responder questões para um simulado já concluído.");
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando tópico pertence a plano de outro usuário")
    void deveLancarForbiddenQuandoTopicoDeOutroUsuario() {
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(outroUsuario).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();
        Topico topico = Topico.builder().id(UUID.randomUUID()).materia(materia).build();

        assertThatThrownBy(() -> validator.validarResolucao(questao, alternativa, 30, null, topico, usuario))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não possui permissão para associar a resposta a este tópico.");
    }
}
