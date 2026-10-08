package com.kyofoundation.skillnapse.modules.gamificacao.support;

import com.kyofoundation.skillnapse.modules.gamificacao.entity.OfensivaUsuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CalculoOfensivaSupport {

    public boolean revalidarStreak(OfensivaUsuario ofensiva, LocalDate dataReferencia) {
        if (ofensiva == null) {
            return false;
        }

        LocalDate hoje = dataReferencia != null ? dataReferencia : LocalDate.now();
        LocalDate ultimoEstudo = ofensiva.getDataUltimoEstudo();

        if (ultimoEstudo == null) {
            if (ofensiva.getDiasConsecutivosAtual() != 0) {
                ofensiva.setDiasConsecutivosAtual(0);
                return true;
            }
            return false;
        }

        // Se o último estudo foi antes de ontem, a sequência foi rompida
        if (ultimoEstudo.isBefore(hoje.minusDays(1))) {
            if (ofensiva.getDiasConsecutivosAtual() != 0) {
                ofensiva.setDiasConsecutivosAtual(0);
                return true;
            }
        }

        return false;
    }

    public void registrarEstudo(OfensivaUsuario ofensiva, LocalDate dataEstudo) {
        if (ofensiva == null || dataEstudo == null) {
            return;
        }

        LocalDate ultimoEstudo = ofensiva.getDataUltimoEstudo();

        if (ultimoEstudo == null) {
            ofensiva.setDiasConsecutivosAtual(1);
            ofensiva.setMaiorSequenciaDias(Math.max(ofensiva.getMaiorSequenciaDias(), 1));
            ofensiva.setDataUltimoEstudo(dataEstudo);
            return;
        }

        if (ultimoEstudo.equals(dataEstudo)) {
            // Estudo no mesmo dia: sequência já computada, mantém inalterada
            return;
        }

        if (ultimoEstudo.equals(dataEstudo.minusDays(1))) {
            // Dia consecutivo perfeito (+1 dia na ofensiva)
            int novaSequencia = ofensiva.getDiasConsecutivosAtual() + 1;
            ofensiva.setDiasConsecutivosAtual(novaSequencia);
            ofensiva.setMaiorSequenciaDias(Math.max(ofensiva.getMaiorSequenciaDias(), novaSequencia));
            ofensiva.setDataUltimoEstudo(dataEstudo);
            return;
        }

        if (ultimoEstudo.isBefore(dataEstudo.minusDays(1))) {
            // Quebra de sequência: recomeça contagem de 1
            ofensiva.setDiasConsecutivosAtual(1);
            ofensiva.setMaiorSequenciaDias(Math.max(ofensiva.getMaiorSequenciaDias(), 1));
            ofensiva.setDataUltimoEstudo(dataEstudo);
            return;
        }

        // Caso dataEstudo seja anterior a ultimoEstudo (registro retroativo):
        // Preserva a ofensiva atual mais recente.
    }

    public boolean isEstudouHoje(OfensivaUsuario ofensiva, LocalDate hoje) {
        if (ofensiva == null || ofensiva.getDataUltimoEstudo() == null) {
            return false;
        }
        LocalDate baseDate = hoje != null ? hoje : LocalDate.now();
        return ofensiva.getDataUltimoEstudo().equals(baseDate);
    }

    public boolean isOfensivaAtiva(OfensivaUsuario ofensiva, LocalDate hoje) {
        if (ofensiva == null || ofensiva.getDataUltimoEstudo() == null || ofensiva.getDiasConsecutivosAtual() == null) {
            return false;
        }
        if (ofensiva.getDiasConsecutivosAtual() <= 0) {
            return false;
        }
        LocalDate baseDate = hoje != null ? hoje : LocalDate.now();
        return !ofensiva.getDataUltimoEstudo().isBefore(baseDate.minusDays(1));
    }
}
