package com.kyofoundation.skillnapse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;

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
    @DisplayName("Deve lançar IllegalStateException quando secret Base64 decodificada tiver menos de 32 bytes")
    void deveLancarExcecaoQuandoSecretBase64TiverMenosDe32BytesDecodificados() {
        // 24 bytes em Base64 resultam exatamente em 32 caracteres ASCII, o que causava o bypass na validação UTF-8
        String secretBase64De24Bytes = Base64.getEncoder().encodeToString(new byte[24]);
        assertThat(secretBase64De24Bytes.length()).isEqualTo(32);

        assertThatThrownBy(() -> securityConfig.secretKey(secretBase64De24Bytes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave secreta Base64 decodificada deve conter pelo menos 256 bits (32 bytes).");

        String secretBase64De16Bytes = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> securityConfig.secretKey(secretBase64De16Bytes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave secreta Base64 decodificada deve conter pelo menos 256 bits (32 bytes).");
    }

    @Test
    @DisplayName("Deve gerar SecretKey HmacSHA256 com sucesso quando secret Base64 tiver 32 bytes decodificados ou mais")
    void deveGerarSecretKeyQuandoSecretBase64Tiver32BytesOuMais() {
        byte[] rawBytes = new byte[32];
        Arrays.fill(rawBytes, (byte) 7);
        String secretBase64 = Base64.getEncoder().encodeToString(rawBytes);

        SecretKey secretKey = securityConfig.secretKey(secretBase64);

        assertThat(secretKey).isNotNull();
        assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(secretKey.getEncoded()).isEqualTo(rawBytes);
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
