package com.kyofoundation.skillnapse.modules.auth.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.common.exception.ForbiddenException;
import com.kyofoundation.skillnapse.common.exception.UnauthorizedException;
import com.kyofoundation.skillnapse.modules.auth.dto.request.LoginRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.request.RegistroRequest;
import com.kyofoundation.skillnapse.modules.auth.entity.Usuario;
import com.kyofoundation.skillnapse.modules.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UsuarioValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int SENHA_MIN_LENGTH = 6;
    private static final int MAX_NOME_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_SENHA_LENGTH = 72;
    private static final int MAX_SENHA_BYTES = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void validarRegistro(RegistroRequest request) {
        if (request == null) {
            throw new BadRequestException("A requisição de cadastro não pode ser nula.");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new BadRequestException("É necessário um nome para se registrar.");
        }
        if (request.nome().trim().length() > MAX_NOME_LENGTH) {
            throw new BadRequestException("O nome não pode ter mais de " + MAX_NOME_LENGTH + " caracteres.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("É necessário um email para se registrar.");
        }
        if (request.email().trim().length() > MAX_EMAIL_LENGTH) {
            throw new BadRequestException("O e-mail não pode ter mais de " + MAX_EMAIL_LENGTH + " caracteres.");
        }
        if (!EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new BadRequestException("O e-mail informado possui um formato inválido.");
        }
        validarEmailDisponivel(request.email());
        if (request.senha() == null || request.senha().isBlank()) {
            throw new BadRequestException("É necessário definir uma senha para se registrar.");
        }
        if (request.senha().trim().length() < SENHA_MIN_LENGTH) {
            throw new BadRequestException("A senha deve conter no mínimo " + SENHA_MIN_LENGTH + " caracteres.");
        }
        if (request.senha().length() > MAX_SENHA_LENGTH || request.senha().getBytes(StandardCharsets.UTF_8).length > MAX_SENHA_BYTES) {
            throw new BadRequestException("A senha não pode ter mais de " + MAX_SENHA_LENGTH + " caracteres.");
        }
    }

    public void validarLogin(LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("A requisição de login não pode ser nula.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("O e-mail é obrigatório para realizar o login.");
        }
        if (request.email().trim().length() > MAX_EMAIL_LENGTH) {
            throw new BadRequestException("O e-mail não pode ter mais de " + MAX_EMAIL_LENGTH + " caracteres.");
        }
        if (request.senha() == null || request.senha().isBlank()) {
            throw new BadRequestException("A senha é obrigatória para realizar o login.");
        }
        if (request.senha().length() > MAX_SENHA_LENGTH || request.senha().getBytes(StandardCharsets.UTF_8).length > MAX_SENHA_BYTES) {
            throw new BadRequestException("A senha não pode ter mais de " + MAX_SENHA_LENGTH + " caracteres.");
        }
    }

    public void validarCredenciais(Usuario usuario, String senhaInformada) {
        if (usuario == null || senhaInformada == null || !passwordEncoder.matches(senhaInformada, usuario.getSenhaHash())) {
            throw new UnauthorizedException("Credenciais inválidas: e-mail ou senha incorretos.");
        }
        validarUsuarioAtivo(usuario);
    }

    public void validarUsuarioAtivo(Usuario usuario) {
        if (usuario != null && Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new ForbiddenException("Usuário inativo ou bloqueado no sistema.");
        }
    }

    public void validarEmailDisponivel(String email) {
        if (email != null && usuarioRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new ConflictException("Já existe um usuário cadastrado com este e-mail.");
        }
    }
}
