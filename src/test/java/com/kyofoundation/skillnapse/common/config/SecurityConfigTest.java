package com.kyofoundation.skillnapse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve lançar IllegalStateException quando secret JWT for nula ou vazia")
    void deveLancarExcecaoQuandoSecretNulaOuVazia(String secretInvalida) {
        assertThatThrownBy(() -> securityConfig.secretKey(secretInvalida))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave secreta JWT (skillnapse.jwt.secret / JWT_SECRET) deve ser configurada.");
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando secret JWT tiver menos de 256 bits (32 bytes)")
    void deveLancarExcecaoQuandoSecretMenorQue256Bits() {
        String chaveCurta = "chave-com-menos-de-32-bytes";
        assertThatThrownBy(() -> securityConfig.secretKey(chaveCurta))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave secreta JWT deve conter pelo menos 256 bits (32 bytes).");
    }

    @Test
    @DisplayName("Deve gerar SecretKey HmacSHA256 com sucesso quando secret tiver 256 bits ou mais")
    void deveGerarSecretKeyComSucesso() {
        String chaveSegura = "chave-secreta-com-pelo-menos-256-bits-de-comprimento-seguro-skillnapse";
        SecretKey secretKey = securityConfig.secretKey(chaveSegura);

        assertThat(secretKey).isNotNull();
        assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    @DisplayName("Deve configurar OpenAPI sem requisito de segurança top-level e com bearerAuth em components")
    void deveConfigurarOpenApiSemSecurityTopLevel() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getSecurity()).isNullOrEmpty();
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey(OpenApiConfig.SECURITY_SCHEME_NAME);
        assertThat(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME).getType().toString())
                .isEqualTo("http");
        assertThat(openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.SECURITY_SCHEME_NAME).getScheme())
                .isEqualTo("bearer");
    }
}
