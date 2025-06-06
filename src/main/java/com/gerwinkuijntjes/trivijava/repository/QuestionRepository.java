package com.gerwinkuijntjes.trivijava.repository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository {

    void storeCorrectAnswer(UUID questionId, UUID correctAnswerId);

    Optional<UUID> getCorrectAnswer(UUID questionId);
}

