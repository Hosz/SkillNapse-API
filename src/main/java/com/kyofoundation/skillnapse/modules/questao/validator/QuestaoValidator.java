package com.kyofoundation.skillnapse.modules.questao.validator;

import com.kyofoundation.skillnapse.common.exception.BadRequestException;
import com.kyofoundation.skillnapse.common.exception.ConflictException;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarAlternativaRequest;
import com.kyofoundation.skillnapse.modules.questao.dto.request.CriarQuestaoRequest;
import com.kyofoundation.skillnapse.modules.questao.finder.QuestaoFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QuestaoValidator {

    private static final int MIN_ALTERNATIVAS = 2;
    private static final int MAX_ALTERNATIVAS = 5;

    private final QuestaoFinder questaoFinder;

    public void validarCriacao(CriarQuestaoRequest request, String hashEnunciado) {
        if (request == null) {
            throw new BadRequestException("Os dados da questão não podem ser nulos.");
        }

        validarAlternativas(request.alternativas());

        if (hashEnunciado != null && questaoFinder.existsByHashEnunciado(hashEnunciado)) {
            throw new ConflictException("Questão com o mesmo enunciado já cadastrada no acervo.");
        }
    }

    private void validarAlternativas(List<CriarAlternativaRequest> alternativas) {
        if (alternativas == null || alternativas.size() < MIN_ALTERNATIVAS || alternativas.size() > MAX_ALTERNATIVAS) {
            throw new BadRequestException("A questão deve conter entre " + MIN_ALTERNATIVAS + " e " + MAX_ALTERNATIVAS + " alternativas.");
        }

        long totalCorretas = alternativas.stream()
                .filter(alt -> alt != null && Boolean.TRUE.equals(alt.correta()))
                .count();

        if (totalCorretas != 1) {
            throw new BadRequestException("A questão deve conter exatamente uma alternativa correta.");
        }

        Set<String> letras = new HashSet<>();
        for (CriarAlternativaRequest alt : alternativas) {
            if (alt == null || alt.letra() == null || alt.letra().isBlank()) {
                throw new BadRequestException("Cada alternativa deve possuir uma letra identificadora.");
            }
            String letraNormalizada = alt.letra().trim().toUpperCase();
            if (!letras.add(letraNormalizada)) {
                throw new BadRequestException("As alternativas não podem possuir letras repetidas.");
            }
        }
    }
}
