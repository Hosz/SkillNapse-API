package com.kyofoundation.skillnapse.modules.auth.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.dto.request.LoginRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.request.RegistroRequest;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioValidatorTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioValidator usuarioValidator;

    @BeforeEach
    void setUp() {
        usuarioValidator = new UsuarioValidator(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Deve validar requisição de registro válida com sucesso")
    void deveValidarRegistroComSucesso() {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", "senha123");
        when(usuarioRepository.existsByEmail("aluno@skillnapse.com")).thenReturn(false);

        assertThatCode(() -> usuarioValidator.validarRegistro(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando requisição de cadastro for nula")
    void deveLancarExcecaoQuandoRequestRegistroForNula() {
        assertThatThrownBy(() -> usuarioValidator.validarRegistro(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A requisição de cadastro não pode ser nula.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar BadRequestException quando nome for nulo ou vazio no cadastro")
    void deveLancarExcecaoQuandoNomeForInvalido(String nomeInvalido) {
        RegistroRequest request = new RegistroRequest(nomeInvalido, "aluno@skillnapse.com", "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("É necessário um nome para se registrar.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar BadRequestException quando e-mail for nulo ou vazio no cadastro")
    void deveLancarExcecaoQuandoEmailForInvalido(String emailInvalido) {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", emailInvalido, "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("É necessário um email para se registrar.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email-invalido", "aluno@", "@dominio.com", "aluno@dominio"})
    @DisplayName("Deve lançar BadRequestException quando formato do e-mail for inválido")
    void deveLancarExcecaoQuandoFormatoEmailInvalido(String emailInvalido) {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", emailInvalido, "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O e-mail informado possui um formato inválido.");
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando e-mail já estiver cadastrado")
    void deveLancarConflictExceptionQuandoEmailJaExistir() {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "existente@skillnapse.com", "senha123");
        when(usuarioRepository.existsByEmail("existente@skillnapse.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar BadRequestException quando senha for nula ou vazia no cadastro")
    void deveLancarExcecaoQuandoSenhaForInvalida(String senhaInvalida) {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", senhaInvalida);
        when(usuarioRepository.existsByEmail("aluno@skillnapse.com")).thenReturn(false);

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("É necessário definir uma senha para se registrar.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando senha possuir menos de 6 caracteres")
    void deveLancarExcecaoQuandoSenhaForCurta() {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", "12345");
        when(usuarioRepository.existsByEmail("aluno@skillnapse.com")).thenReturn(false);

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A senha deve conter no mínimo 6 caracteres.");
    }

    @Test
    @DisplayName("Deve validar requisição de login válida com sucesso")
    void deveValidarLoginComSucesso() {
        LoginRequest request = new LoginRequest("aluno@skillnapse.com", "senha123");

        assertThatCode(() -> usuarioValidator.validarLogin(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando requisição de login for nula")
    void deveLancarExcecaoQuandoRequestLoginForNula() {
        assertThatThrownBy(() -> usuarioValidator.validarLogin(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A requisição de login não pode ser nula.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar BadRequestException quando e-mail for inválido no login")
    void deveLancarExcecaoQuandoEmailLoginForInvalido(String emailInvalido) {
        LoginRequest request = new LoginRequest(emailInvalido, "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O e-mail é obrigatório para realizar o login.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar BadRequestException quando senha for inválida no login")
    void deveLancarExcecaoQuandoSenhaLoginForInvalida(String senhaInvalida) {
        LoginRequest request = new LoginRequest("aluno@skillnapse.com", senhaInvalida);

        assertThatThrownBy(() -> usuarioValidator.validarLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A senha é obrigatória para realizar o login.");
    }

    @Test
    @DisplayName("Deve validar credenciais corretas de usuário ativo com sucesso")
    void deveValidarCredenciaisComSucesso() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .senhaHash("hash-seguro")
                .ativo(true)
                .build();

        when(passwordEncoder.matches("senha123", "hash-seguro")).thenReturn(true);

        assertThatCode(() -> usuarioValidator.validarCredenciais(usuario, "senha123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando senha estiver incorreta")
    void deveLancarUnauthorizedExceptionQuandoSenhaIncorreta() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .senhaHash("hash-seguro")
                .ativo(true)
                .build();

        when(passwordEncoder.matches("senha-errada", "hash-seguro")).thenReturn(false);

        assertThatThrownBy(() -> usuarioValidator.validarCredenciais(usuario, "senha-errada"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas: e-mail ou senha incorretos.");
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando usuário com credenciais corretas estiver inativo")
    void deveLancarForbiddenExceptionQuandoUsuarioEstiverInativo() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno Kyo")
                .email("aluno@skillnapse.com")
                .senhaHash("hash-seguro")
                .ativo(false)
                .build();

        when(passwordEncoder.matches("senha123", "hash-seguro")).thenReturn(true);

        assertThatThrownBy(() -> usuarioValidator.validarCredenciais(usuario, "senha123"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Usuário inativo ou bloqueado no sistema.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando nome exceder 150 caracteres no cadastro")
    void deveLancarExcecaoQuandoNomeExceder150Caracteres() {
        String nomeLongo = "A".repeat(151);
        RegistroRequest request = new RegistroRequest(nomeLongo, "aluno@skillnapse.com", "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O nome não pode ter mais de 150 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando email exceder 150 caracteres no cadastro")
    void deveLancarExcecaoQuandoEmailExceder150CaracteresNoCadastro() {
        String emailLongo = "a".repeat(140) + "@skillnapse.com";
        RegistroRequest request = new RegistroRequest("Aluno Kyo", emailLongo, "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O e-mail não pode ter mais de 150 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando senha exceder 72 caracteres no cadastro")
    void deveLancarExcecaoQuandoSenhaExceder72CaracteresNoCadastro() {
        String senhaLonga = "s".repeat(73);
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", senhaLonga);
        when(usuarioRepository.existsByEmail("aluno@skillnapse.com")).thenReturn(false);

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A senha não pode ter mais de 72 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando senha exceder 72 bytes em UTF-8 no cadastro")
    void deveLancarExcecaoQuandoSenhaExceder72BytesNoCadastro() {
        // 25 emojis de 4 bytes = 100 bytes (> 72 bytes), apesar de ter apenas 25 caracteres visuais
        String senhaMultiByte = "🔐".repeat(25);
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", senhaMultiByte);
        when(usuarioRepository.existsByEmail("aluno@skillnapse.com")).thenReturn(false);

        assertThatThrownBy(() -> usuarioValidator.validarRegistro(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A senha não pode ter mais de 72 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando email exceder 150 caracteres no login")
    void deveLancarExcecaoQuandoEmailExceder150CaracteresNoLogin() {
        String emailLongo = "a".repeat(140) + "@skillnapse.com";
        LoginRequest request = new LoginRequest(emailLongo, "senha123");

        assertThatThrownBy(() -> usuarioValidator.validarLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O e-mail não pode ter mais de 150 caracteres.");
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando senha exceder 72 caracteres no login")
    void deveLancarExcecaoQuandoSenhaExceder72CaracteresNoLogin() {
        String senhaLonga = "s".repeat(73);
        LoginRequest request = new LoginRequest("aluno@skillnapse.com", senhaLonga);

        assertThatThrownBy(() -> usuarioValidator.validarLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A senha não pode ter mais de 72 caracteres.");
    }
}
