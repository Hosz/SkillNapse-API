package com.kyofoundation.skillnapse.modules.flashcard.repository;

import java.util.UUID;

public interface BaralhoMetricasProjection {

    UUID getBaralhoId();

    Long getTotalCards();

    Long getTotalCardsParaRevisar();
}
