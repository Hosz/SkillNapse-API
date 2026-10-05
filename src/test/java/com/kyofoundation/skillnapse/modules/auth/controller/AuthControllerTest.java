package com.kyofoundation.skillnapse.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyofoundation.skillnapse.common.config.SecurityConfig;
import com.kyofoundation.skillnapse.common.exception.GlobalExceptionHandler;
import com.kyofoundation.skillnapse.modules.auth.dto.request.LoginRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.request.RegistroRequest;
import com.kyofoundation.skillnapse.modules.auth.dto.response.LoginResponse;
import com.kyofoundation.skillnapse.modules.auth.dto.response.RegistroResponse;
import com.kyofoundation.skillnapse.modules.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("Deve registrar usuario e retornar 200 OK")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        RegistroRequest request = new RegistroRequest("Aluno Kyo", "aluno@skillnapse.com", "senhaSegura123");
        RegistroResponse response = new RegistroResponse(UUID.randomUUID(), "Aluno Kyo", "aluno@skillnapse.com");

        when(authService.register(any(RegistroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno Kyo"))
                .andExpect(jsonPath("$.email").value("aluno@skillnapse.com"));
    }

    @Test
    @DisplayName("Deve logar usuario e retornar 200 OK com tokens")
    void deveLogarUsuarioComSucesso() throws Exception {
        LoginRequest request = new LoginRequest("aluno@skillnapse.com", "senhaSegura123");
        LoginResponse response = new LoginResponse(
                UUID.randomUUID(),
                "Aluno Kyo",
                "aluno@skillnapse.com",
                "mock-access-token",
                "mock-refresh-token",
                "Bearer",
                3600L
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
