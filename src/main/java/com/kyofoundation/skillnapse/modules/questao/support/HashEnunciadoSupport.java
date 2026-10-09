package com.kyofoundation.skillnapse.modules.questao.support;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashEnunciadoSupport {

    public String gerarHash(String enunciado) {
        if (enunciado == null || enunciado.isBlank()) {
            return "";
        }

        // Normalização: trim, minúsculas e redução de múltiplos espaços em branco para 1 espaço
        String normalizado = enunciado.trim().toLowerCase().replaceAll("\\s+", " ");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalizado.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash SHA-256 indisponível no ambiente Java.", e);
        }
    }
}
