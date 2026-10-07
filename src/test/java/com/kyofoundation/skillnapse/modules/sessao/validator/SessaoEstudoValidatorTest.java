package com.kyofoundation.skillnapse.modules.sessao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Materia;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.PlanoEstudo;
import com.kyofoundation.skillnapse.modules.planoestudo.entity.Topico;
import com.kyofoundation.skillnapse.modules.sessao.dto.request.RegistrarSessaoEstudoRequest;
import com.kyofoundation.skillnapse.modules.sessao.entity.SessaoEstudo;
import com.kyofoundation.skillnapse.modules.sessao.enums.StatusSessaoEstudo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessaoEstudoValidatorTest {

    private SessaoEstudoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SessaoEstudoValidator();
    }

    private Usuario criarUsuario(UUID id) {
        return Usuario.builder().id(id).email("aluno@skillnapse.com").build();
    }

    private Topico criarTopico(Usuario dono) {
        PlanoEstudo plano = PlanoEstudo.builder().id(UUID.randomUUID()).usuario(dono).build();
        Materia materia = Materia.builder().id(UUID.randomUUID()).planoEstudo(plano).build();
        return Topico.builder().id(UUID.randomUUID()).materia(materia).titulo("Sintaxe Básica").build();
    }

    @Test
    @DisplayName("Deve validar registro com sucesso quando todos os dados estiverem consistentes")
    void deveValidarRegistroComSucesso() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Topico topico = criarTopico(usuario);

        Instant inicio = Instant.now().minus(50, ChronoUnit.MINUTES);
        Instant fim = Instant.now();

        RegistrarSessaoEstudoRequest request = new RegistrarSessaoEstudoRequest(
                topico.getId(),
                inicio,
                fim,
                2700, // 45 min líquidos num intervalo de 50 min
                StatusSessaoEstudo.CONCLUIDA,
                "Ciclo focado de resolução"
        );

        assertThatCode(() -> validator.validarRegistro(request, usuario, topico))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar se o request for nulo")
    void deveFalharSeRequestNulo() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Topico topico = criarTopico(usuario);

        assertThatThrownBy(() -> validator.validarRegistro(null, usuario, topico))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não podem ser nulos");
    }

    @Test
    @DisplayName("Deve falhar se horario de termino nao for posterior ao inicio")
    void deveFalharSeTerminoAntesDoInicio() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Topico topico = criarTopico(usuario);

        Instant agora = Instant.now();
        RegistrarSessaoEstudoRequest request = new RegistrarSessaoEstudoRequest(
                topico.getId(),
                agora,
                agora.minus(10, ChronoUnit.MINUTES),
                1800,
                StatusSessaoEstudo.CONCLUIDA,
                null
        );

        assertThatThrownBy(() -> validator.validarRegistro(request, usuario, topico))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("término deve ser posterior ao horário de início");
    }

    @Test
    @DisplayName("Deve falhar se a duracao liquida for maior que o tempo bruto decorrido")
    void deveFalharSeDuracaoLiquidaExcederIntervaloBruto() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Topico topico = criarTopico(usuario);

        Instant inicio = Instant.now().minus(30, ChronoUnit.MINUTES); // 1800 segundos
        Instant fim = Instant.now();

        RegistrarSessaoEstudoRequest request = new RegistrarSessaoEstudoRequest(
                topico.getId(),
                inicio,
                fim,
                2000, // 2000s > 1800s
                StatusSessaoEstudo.CONCLUIDA,
                null
        );

        assertThatThrownBy(() -> validator.validarRegistro(request, usuario, topico))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("superior ao intervalo total");
    }

    @Test
    @DisplayName("Deve falhar se topico pertencer a outro usuario")
    void deveFalharSeTopicoDeOutroUsuario() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Usuario outroUsuario = criarUsuario(UUID.randomUUID());
        Topico topicoDeOutro = criarTopico(outroUsuario);

        Instant inicio = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant fim = Instant.now();

        RegistrarSessaoEstudoRequest request = new RegistrarSessaoEstudoRequest(
                topicoDeOutro.getId(),
                inicio,
                fim,
                1500,
                StatusSessaoEstudo.CONCLUIDA,
                null
        );

        assertThatThrownBy(() -> validator.validarRegistro(request, usuario, topicoDeOutro))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permissão");
    }

    @Test
    @DisplayName("Deve validar propriedade da sessao com sucesso para o proprietario")
    void deveValidarPropriedadeSessaoComSucesso() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        SessaoEstudo sessao = SessaoEstudo.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();

        assertThatCode(() -> validator.validarPropriedadeSessao(usuario, sessao))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar propriedade da sessao para outro usuario")
    void deveFalharPropriedadeSessaoParaOutroUsuario() {
        Usuario usuario = criarUsuario(UUID.randomUUID());
        Usuario invasor = criarUsuario(UUID.randomUUID());
        SessaoEstudo sessao = SessaoEstudo.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();

        assertThatThrownBy(() -> validator.validarPropriedadeSessao(invasor, sessao))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("não possui permissão");
    }

    @Test
    @DisplayName("Deve validar filtro de periodo com sucesso ou lancar erro se datas invertidas")
    void deveValidarFiltroPeriodo() {
        Instant agora = Instant.now();
        Instant ontem = agora.minus(1, ChronoUnit.DAYS);

        assertThatCode(() -> validator.validarFiltroPeriodo(ontem, agora)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarFiltroPeriodo(null, agora)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validarFiltroPeriodo(null, null)).doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validarFiltroPeriodo(agora, ontem))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não pode ser posterior");
    }
}
